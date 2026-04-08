package dingwan.easy.ai.app;

import dingwan.easy.ai.agent.impl.SimpleAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.tool.ToolDefinition;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
public class SimpleAgentTest {

    @Autowired ChatClient chatClient;
    @Autowired ToolExecutor toolExecutor;
    @Autowired ToolRegistry toolRegistry;

    // 基础对话
    @Test
    public void testChat() {
        SimpleAgent simpleAgent = new SimpleAgent("基础助手", chatClient, "你是一个友好的AI助手，请用简洁明了的方式回答问题。",
                null, null, null, false, 3);
        String run = simpleAgent.run("你好，请介绍一下自己", null);
        System.out.println(run);
    }

    // 基础流式对话
    @Test
    public void testChatStream() {
        SimpleAgent simpleAgent = new SimpleAgent("基础助手", chatClient, "你是一个友好的AI助手，请用简洁明了的方式回答问题。",
                null, null, null, false, 3);
        Flux<ChatResponse> responseFlux = simpleAgent.runStream("你好，请介绍一下自己", null);
        responseFlux.subscribe(System.out::println);
    }

    // 带工具测试
    @Test
    public void testTool() {
        System.out.println("已注册工具数: " + toolRegistry.getToolCount());
        SimpleAgent agent = new SimpleAgent("增强助手", chatClient, "你是一个智能助手，可以使用工具来帮助用户。",
                null, toolRegistry, toolExecutor, true, 8);
        String run = agent.run("请帮我计算 15 * 8 + 32 必须调用工具解决");
        System.out.println(run);
    }

}
