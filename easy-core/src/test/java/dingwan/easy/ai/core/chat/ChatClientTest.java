package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.core.config.EasyAiProperties;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatClientTest {

    @Autowired ChatClient chatClient;

    @Autowired EasyAiProperties properties;

    @Test
    public void test1() {
        System.out.println(">>> baseUrl = " + properties.getBaseUrl());
    }

    @Test
    void test() {
        ChatResponse chatResponse = chatClient.call(ChatRequest.of(UserMessage.builder().text("你好").build()));
        System.out.println(chatResponse);
    }

}