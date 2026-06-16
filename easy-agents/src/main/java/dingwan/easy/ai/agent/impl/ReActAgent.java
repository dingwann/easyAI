package dingwan.easy.ai.agent.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.agent.model.ReActOutput;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.Message;
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
        return "";
    }

    @Override
    public String run(String inputText, Map<String, Object> kwargs) {
        List<String> currentHistory = new ArrayList<>();
        int currentStep = 0;
        log.info("\n🤖 {} 开始处理问题：{}", this.name, inputText);

        while (currentStep < this.maxToolIteration) {
            currentStep++;
            log.info("\n--- 第{}步 ---", currentStep);
            // 构建提示词
            String allToolDesc = this.toolRegistry.getAllToolDesc();
            String historyStr = String.join("\n", currentHistory);
            String prompt = this.systemPrompt.replace("{tools}", allToolDesc)
                    .replace("{history}", historyStr)
                    .replace("{question", inputText);

            // 调用LLM
            UserMessage userMessage = UserMessage.builder()
                    .text(prompt)
                    .build();
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(List.of(userMessage))
                    .options(ChatOptions.builderByArgs(kwargs))
                    .build();
            String responseContent = this.chatClient.call(chatRequest).getContent();

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


        }






        return "";
    }

    private ReActOutput parseOutput(String responseContent) {
        Matcher matcher = jsonPattern.matcher(responseContent);
        if (!matcher.find()) {
            log.error("❌ AI回答JSON解析错误");
            throw new RuntimeException("AI回答JSON解析错误");
            // TODO 错误兜底处理
        }
        String chatStr = matcher.group(1).trim();
        ReActOutput output;
        try {
            output = OBJECT_MAPPER.readValue(chatStr, ReActOutput.class);
        } catch (JsonProcessingException e) {
            // TODO 错误兜底处理
            throw new RuntimeException(e);
        }
        return output;
    }

}
