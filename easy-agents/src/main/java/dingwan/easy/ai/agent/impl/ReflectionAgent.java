package dingwan.easy.ai.agent.impl;

import dingwan.easy.ai.agent.config.PromptTemplate;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.SystemMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ReflectionAgent {

    private ChatClient initChatClient;
    private ChatClient reflectionChatClient;
    private ChatClient refineChatClient;

    public ReflectionAgent(ChatClient initChatClient, ChatClient reflectionChatClient, ChatClient refineChatClient) {
        this.initChatClient = initChatClient;
        this.reflectionChatClient = reflectionChatClient;
        this.refineChatClient = refineChatClient;
    }

    public String run(String inputText) {
        return this.run(inputText, null);
    }

    public String run(String inputText, Map<String, Object> kwargs) {
        log.info("原始问题：{}", inputText);
        // 执行初始问题
        // 构建提示词
        String initPrompt = PromptTemplate.Reflection_init
                .replace("{task}", inputText);
        ChatRequest chatRequest = ChatRequest.of(
                UserMessage.builder()
                        .text(inputText)
                        .build()
        );
        String lastResponse = this.initChatClient.call(chatRequest).getContent();
        log.info("初始问题响应结果：{}", lastResponse);
        // 审查
        String reflectPrompt = PromptTemplate.Reflection_reflect
                .replace("{task}", inputText)
                .replace("{content}", lastResponse);
        String reflectionResponse = this.reflectionChatClient.call(reflectPrompt).getContent();
        int reflectionIndex = 1;
        log.info("第 {} 次审查，结果：{}", reflectionIndex++, reflectionResponse);
        int index = 1;
        while (!reflectionResponse.contains("无需改进")) {
            // 改进
            String refinePrompt = PromptTemplate.Reflection_refine
                    .replace("{task}", inputText)
                    .replace("{last_attempt}", lastResponse)
                    .replace("{feedback}", reflectionResponse);
            String refineChatResponse = this.refineChatClient.call(refinePrompt).getContent();
            log.info("第 {} 次改进，结果：{}", index++, refineChatResponse);
            // 审查
            reflectPrompt = PromptTemplate.Reflection_reflect
                    .replace("{task}", inputText)
                    .replace("{content}", refineChatResponse);
            reflectionResponse = this.reflectionChatClient.call(reflectPrompt).getContent();
            log.info("第 {} 次审查，结果：{}", reflectionIndex++, reflectionResponse);
            lastResponse = refineChatResponse;
        }
        log.info("执行完成🙂 \n{}", lastResponse);
        return lastResponse;
    }

}
