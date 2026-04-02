package dingwan.easy.ai.agent.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.SystemMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.tool.ToolCallJSON;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于基类重写简单对话Agent
 */
@Slf4j
public class SimpleAgent extends BaseAgent {

    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final boolean toolEnabled;
    private final int maxToolIteration;
    private static final Pattern jsonPattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");

    public SimpleAgent(String name, ChatClient chatClient, String systemPrompt, ChatOptions chatOptions, ToolRegistry toolRegistry, ToolExecutor toolExecutor, boolean toolEnabled, int maxToolIteration) {
        super(name, chatClient, systemPrompt, chatOptions);
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.toolEnabled = toolEnabled;
        this.maxToolIteration = maxToolIteration;
        log.info("✅ {} 初始化完成，工具调用: {}", this.name, this.toolEnabled ? "启用" : "禁用");
    }

    /**
     * 重写的运行方法 - 实现简单对话逻辑，支持可选工具调用
     * @param inputText 输入问题
     * @param kwargs 参数
     * @return 答案
     */
    @Override
    public String run(String inputText, Map<String, Object> kwargs) {
        log.info("\uD83E\uDD16 {} 正在处理: {}", this.name, inputText);
        // 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 添加系统消息
        String systemPrompt = this.getSystemPrompt();
        messages.add(new SystemMessage(systemPrompt));

        // 添加历史消息
        messages.addAll(this.history);

        // 添加用户消息
        messages.add(new UserMessage(inputText));

        // 如果没有启用工具调用，使用简单的对话逻辑
        if (!this.toolEnabled) {
            ChatResponse chatResponse = this.chatClient.call(messages);
            this.addMessage(new UserMessage(inputText));
            this.addMessage(new AssistantMessage(chatResponse.getContent()));
            log.info("✅ {} 响应完成", this.name);
            return chatResponse.getContent();
        }

        // 支持多轮工具调用的逻辑
        return this.runWithTool(messages, inputText, maxToolIteration, kwargs);
    }

    /**
     * 流式运行方法 - 实现流式对话，支持可选工具调用
     * @param inputText 输入问题
     * @param kwargs 参数
     * @return 流式响应
     */
    public Flux<ChatResponse> runStream(String inputText, Map<String, Object> kwargs) {
        log.info("\uD83E\uDD16 {} 正在处理(流式): {}", this.name, inputText);

        // 构建消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(this.getSystemPrompt()));
        messages.addAll(this.history);
        messages.add(new UserMessage(inputText));

        // 如果不启用工具，直接流式返回
        if (!this.toolEnabled) {
            return this.chatClient.stream(messages)
                    .doOnComplete(() -> {
                        this.addMessage(new UserMessage(inputText));
                        log.info("✅ {} 流式响应完成", this.name);
                    });
        }

        // 启用工具时，需要收集完整响应后处理工具调用
        return runStreamWithTool(messages, inputText, kwargs);
    }

    /**
     * 流式工具调用逻辑
     */
    private Flux<ChatResponse> runStreamWithTool(List<Message> messages, String inputText, Map<String, Object> kwargs) {
        StringBuilder contentBuilder = new StringBuilder();

        return this.chatClient.stream(messages)
                .doOnNext(response -> contentBuilder.append(response.getContent()))
                .concatWith(Flux.defer(() -> {
                    String fullContent = contentBuilder.toString();
                    List<ToolCallJSON> toolCalls = this.parseToolCall(fullContent);

                    if (toolCalls == null || toolCalls.isEmpty()) {
                        // 无工具调用，保存历史并结束
                        this.history.add(new UserMessage(inputText));
                        this.history.add(new AssistantMessage(fullContent));
                        log.info("✅ {} 流式响应完成", this.name);
                        return Flux.empty();
                    }

                    // 有工具调用，执行工具并继续流式返回
                    log.info("\uD83D\uDD27 检测到 {} 个工具调用", toolCalls.size());
                    String[] toolResults = new String[toolCalls.size()];

                    for (int i = 0; i < toolCalls.size(); i++) {
                        ToolCallJSON toolCall = toolCalls.get(i);
                        try {
                            toolResults[i] = toolExecutor.execute(toolCall.getName(), toolCall.getArguments()).toString();
                        } catch (Exception e) {
                            log.info("❌ [{}]工具调用执行出错", toolCall.getName(), e);
                            toolResults[i] = "工具执行出错: " + e.getMessage();
                        }
                    }

                    // 构建新消息继续对话
                    String cleanContent = fullContent.replaceAll("```json\\s*[\\s\\S]*?\\s*```", "");
                    messages.add(new AssistantMessage(cleanContent));
                    messages.add(new UserMessage(String.format("工具执行结果:\n%s\n\n请基于这些结果给出完整的回答。", String.join("\n\n", toolResults))));

                    // 递归调用流式处理
                    return runStreamWithToolRecursive(messages, inputText, 1, kwargs);
                }));
    }

    /**
     * 递归处理流式工具调用
     */
    private Flux<ChatResponse> runStreamWithToolRecursive(List<Message> messages, String inputText, int iteration, Map<String, Object> kwargs) {
        if (iteration >= maxToolIteration) {
            // 达到最大迭代次数，获取最终答案
            StringBuilder contentBuilder = new StringBuilder();
            return this.chatClient.stream(messages)
                    .doOnNext(response -> contentBuilder.append(response.getContent()))
                    .doOnComplete(() -> {
                        this.history.add(new UserMessage(inputText));
                        this.history.add(new AssistantMessage(contentBuilder.toString()));
                        log.info("✅ {} 流式响应完成(达到最大迭代次数)", this.name);
                    });
        }

        StringBuilder contentBuilder = new StringBuilder();

        return this.chatClient.stream(messages)
                .doOnNext(response -> contentBuilder.append(response.getContent()))
                .concatWith(Flux.defer(() -> {
                    String fullContent = contentBuilder.toString();
                    List<ToolCallJSON> toolCalls = this.parseToolCall(fullContent);

                    if (toolCalls == null || toolCalls.isEmpty()) {
                        this.history.add(new UserMessage(inputText));
                        this.history.add(new AssistantMessage(fullContent));
                        log.info("✅ {} 流式响应完成", this.name);
                        return Flux.empty();
                    }

                    log.info("\uD83D\uDD27 检测到 {} 个工具调用", toolCalls.size());
                    String[] toolResults = new String[toolCalls.size()];

                    for (int i = 0; i < toolCalls.size(); i++) {
                        ToolCallJSON toolCall = toolCalls.get(i);
                        try {
                            toolResults[i] = toolExecutor.execute(toolCall.getName(), toolCall.getArguments()).toString();
                        } catch (Exception e) {
                            log.info("❌ [{}]工具调用执行出错", toolCall.getName(), e);
                            toolResults[i] = "工具执行出错: " + e.getMessage();
                        }
                    }

                    String cleanContent = fullContent.replaceAll("```json\\s*[\\s\\S]*?\\s*```", "");
                    messages.add(new AssistantMessage(cleanContent));
                    messages.add(new UserMessage(String.format("工具执行结果:\n%s\n\n请基于这些结果给出完整的回答。", String.join("\n\n", toolResults))));

                    return runStreamWithToolRecursive(messages, inputText, iteration + 1, kwargs);
                }));
    }

    /**
     * 支持工具调用的逻辑
     * @param messages 构建的消息列表
     * @param inputText 用户问题
     * @param maxToolIteration 最大迭代次数
     * @param kwargs 工具参数
     * @return 答案
     */
    private String runWithTool(List<Message> messages, String inputText, int maxToolIteration, Map<String, Object> kwargs) {
        int currentIteration = 0;
        String finalAnswer = "";

        while (currentIteration < maxToolIteration) {
            // 调用llm
            ChatResponse chatResponse = this.chatClient.call(messages);
            // 检查是否有工具调用
            List<ToolCallJSON> toolCallJSON = this.parseToolCall(chatResponse.getContent());
            if (toolCallJSON == null ||  toolCallJSON.isEmpty()) {
                finalAnswer = chatResponse.getContent();
                break;
            }
            // 批量执行工具
            log.info("\uD83D\uDD27 检测到 {} 个工具调用", toolCallJSON.size());

            String[] toolResult = new String[toolCallJSON.size()];
            String cleanResponse = chatResponse.getContent();

            for (int i = 0; i < toolCallJSON.size(); i++) {
                ToolCallJSON toolCall = toolCallJSON.get(i);
                String toolName = toolCall.getName();
                Map<String, Object> arguments = toolCall.getArguments();
                try {
                    String result = toolExecutor.execute(toolName, arguments).toString();
                    toolResult[i] = result;
                } catch (Exception e) {
                    log.info("❌ [{}]工具调用执行出错", toolName, e);
                }
            }
            // 移除工具调用的标记
            cleanResponse = cleanResponse.replaceAll("```json\\s*[\\s\\S]*?\\s*```", "");

            // 构建包含工具结果的消息
            messages.add(new AssistantMessage(cleanResponse));
            // 添加工具结果
            String toolResultsText = String.join("\n\n", toolResult);
            messages.add(new UserMessage(String.format("工具执行结果:\n%s\n\n请基于这些结果给出完整的回答。", toolResultsText)));

            currentIteration++;
        }
        // 如果超过最大迭代次数 获取最后一次回答
        if (currentIteration >= maxToolIteration) {
            finalAnswer = this.chatClient.call(messages).getContent();
        }
        // 保存到历史记录
        this.history.add(new UserMessage(inputText));
        this.history.add(new AssistantMessage(finalAnswer));
        log.info("✅ {} 响应完成", this.name);

        return finalAnswer;
    }

    private List<ToolCallJSON> parseToolCall(String content) {
        // 检查是否有```json区域块
        Matcher matcher = jsonPattern.matcher(content);
        if (!matcher.find())
            return Collections.emptyList();
        String toolStr = matcher.group(1).trim();
        // 将json字符串转换为对象
        return JSON.parseObject(toolStr)
                .getJSONArray("tool_call")
                .stream()
                .map(obj -> ((JSONObject) obj).to(ToolCallJSON.class))
                .toList();
    }

    /**
     * 构建增强的系统提示词，包含工具信息
     * @return 系统提示词
     */
    private String getSystemPrompt() {
        String basePrompt = (this.systemPrompt == null || this.systemPrompt.isEmpty())
                ? "你是一个有用的AI助手。" : this.systemPrompt;

        if (!this.toolEnabled || this.toolRegistry == null) {
            return basePrompt;
        }

        // 获取工具描述
        String toolDesc = this.toolRegistry.getAllToolDesc();
        if (toolDesc == null || "暂无可用工具".equals(toolDesc))
            return basePrompt;

        String toolsSection = """
        ## 可用工具
        你可以使用以下工具来帮助回答问题:
        %s

        ## 工具调用规范
        当你需要调用多个工具时，必须返回一个JSON数组，而不是单个对象。

        格式如下：
        ```json
        {
          "tool_call": [
            {
              "name": "工具名称",
              "arguments": {"参数名": "参数值"}
            }
          ]
        }
        ```

        示例(多个工具)：
        ```json
        {
          "tool_calls": [
            {
              "name": "search",
              "arguments": {"query": "Python编程"}
            },
            {
              "name": "get_weather",
              "arguments": {"city": "北京"}
            }
          ]
        }
        ```

        注意：
        1. 可以返回一个或多个工具调用
        2. 必须是合法JSON
        3. 不要输出额外解释
        4. 如果不需要调用工具，正常回答即可

        工具调用结果会自动插入到对话中，然后你可以基于结果继续回答。
        """.formatted(toolDesc);

        return basePrompt + toolsSection;
    }

}
