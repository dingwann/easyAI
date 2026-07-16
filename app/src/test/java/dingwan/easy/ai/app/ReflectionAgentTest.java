package dingwan.easy.ai.app;

import dingwan.easy.ai.agent.impl.ReflectionAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReflectionAgentTest {

    @Autowired ChatClient chatClient;

    @Test
    public void test() {
        ReflectionAgent reflectionAgent = new ReflectionAgent(chatClient, chatClient, chatClient);
        String run = reflectionAgent.run("写一篇关于人工智能发展历程的简短文章");
    }

}
