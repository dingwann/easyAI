package dingwan.easy.ai.agent.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.agent.model.ReActOutput;
import dingwan.easy.ai.agent.model.ToolCall;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ReActAgent extends BaseAgent {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern jsonPattern = Pattern.compile("```json\\s*([\\s\\S]*?)\\s*```");

    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final int maxToolIteration;

    public ReActAgent(ChatClient chatClient, String systemPrompt, ChatOptions chatOptions, ToolRegistry toolRegistry, ToolExecutor toolExecutor, int maxToolIteration) {
        super("ReAct", chatClient, systemPrompt, chatOptions);
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.maxToolIteration = maxToolIteration;
        log.info("✅ {} 初始化完成，最大步数：{{}}", this.name, this.maxToolIteration);
    }

    @Override
    public String run(String inputText) {
        return this.run(inputText, null);
    }

    @Override
    public String run(String inputText, Map<String, Object> kwargs) {
        List<String> currentHistory = new ArrayList<>();
        int currentStep = 0;
        log.info("\n🤖 {} 开始处理问题：{}", this.name, inputText);

        String allToolDesc = this.toolRegistry.getAllToolDesc();
        while (currentStep < this.maxToolIteration) {
            currentStep++;
            log.info("\n--- 第{}步 ---", currentStep);
            // 构建提示词
            String historyStr = String.join("\n", currentHistory);
            String prompt = this.systemPrompt.replace("{tools}", allToolDesc)
                    .replace("{history}", historyStr)
                    .replace("{question}", inputText);

            // 调用LLM
            UserMessage userMessage = UserMessage.builder()
                    .text(prompt)
                    .build();
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(userMessage))
                    .options(ChatOptions.builderByArgs(kwargs))
                    .build();
            String responseContent = this.chatClient.call(chatRequest).getContent();
            log.info("AI原始输出: {}", responseContent);

            // 解析输出
            ReActOutput output = this.parseOutput(responseContent);

            // 检查完成条件
            String finalAnswer = output.getFinalAnswer();
            if (finalAnswer != null) {
                // 已完成任务
                this.addMessage(UserMessage.builder().text(inputText).build());
                this.addMessage(AssistantMessage.builder().content(finalAnswer).build());
                return finalAnswer;
            }
            // 执行工具调用
            String thought = output.getThought();
            log.info("AI思考内容：{}", thought);
            List<ToolCall> toolCalls = output.getToolCalls();
            // 构建行动消息
            currentHistory.add(String.format("Action: %s", toolCalls.toString()));
            for (ToolCall toolCall : toolCalls) {
                log.info("解析执行工具调用，当前执行工具：{}", toolCall.getName());
                String name = toolCall.getName();
                Map<String, Object> arguments = toolCall.getArguments();
                Object execute;
                try {
                    execute = toolExecutor.execute(name, arguments);
                    // 构建工具消息
                    currentHistory.add(String.format("Observation: %s", execute.toString()));
                } catch (Exception e) {
                    log.error("工具执行失败：{}", name);
                    throw new RuntimeException(e);
                }
            }
        }
        // 达到最大步数
        String finalAnswer = "抱歉，我无法在限定步数内完成这个任务。";
        this.addMessage(UserMessage.builder().text(inputText).build());
        this.addMessage(AssistantMessage.builder().content(finalAnswer).build());
        return finalAnswer;
    }

    private ReActOutput parseOutput(String response) {
        String json = response.trim();
        if (json.startsWith("```")) {
            Matcher matcher = jsonPattern.matcher(json);
            if (matcher.find()) {
                json = matcher.group(1).trim();
            }
        }
        try {
            return OBJECT_MAPPER.readValue(json, ReActOutput.class);
        } catch (Exception e) {
            throw new RuntimeException("解析AI输出失败:\n" + json, e);
        }
    }

}
