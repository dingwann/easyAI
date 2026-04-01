package dingwan.easy.ai.agent.impl;

import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.SystemMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 基于基类重写简单对话Agent
 */
@Slf4j
public class SimpleAgent extends BaseAgent {

    private final ToolRegistry toolRegistry;
    private final boolean toolEnabled;
    private final int maxToolIteration;

    public SimpleAgent(String name, ChatClient chatClient, String systemPrompt, ChatOptions chatOptions, ToolRegistry toolRegistry, boolean toolEnabled, int maxToolIteration) {
        super(name, chatClient, systemPrompt, chatOptions);
        this.toolRegistry = toolRegistry;
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
            this.parseToolCall(chatResponse.getContent());
        }

        return "";
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
        当你需要调用工具时，必须返回一个JSON对象，而不是文本。

        格式如下：
        ```json
        {
          "tool_call": {
            "name": "工具名称",
            "arguments": {"参数名": "参数值"}
          }
        }
        ```

        示例：
        ```json
        {
          "tool_call": {
            "name": "search",
            "arguments": {"query": "Python编程"}
          }
        }
        ```

        注意：
        1. 必须是合法JSON
        2. 不要输出额外解释
        3. 如果不需要调用工具，正常回答即可

        工具调用结果会自动插入到对话中，然后你可以基于结果继续回答。
        """.formatted(toolDesc);

        return basePrompt + toolsSection;
    }

}
