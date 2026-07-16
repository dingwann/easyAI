package dingwan.easy.ai.app;

import dingwan.easy.ai.agent.config.PromptTemplate;
import dingwan.easy.ai.agent.impl.ReActAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReActAgentTest {

    @Autowired ChatClient chatClient;
    @Autowired ToolExecutor toolExecutor;
    @Autowired ToolRegistry toolRegistry;

    // 不带调用工具的执行
    @Test
    public void testChat() {
        ReActAgent reActAgent = new ReActAgent(chatClient, PromptTemplate.ReAct, null, toolRegistry, toolExecutor, 8);
        String run = reActAgent.run("你好，写个java版本的排序算法");
        System.out.println(run);
    }

    // 单个带工具的
    @Test
    public void testWithTool() {
        ReActAgent reActAgent = new ReActAgent(chatClient, PromptTemplate.ReAct, null, toolRegistry, toolExecutor, 8);
        String run = reActAgent.run("现在时间是多少？");
        System.out.println(run);
    }

    // 多个工具执行
    @Test
    public void testWithTools() {
        ReActAgent reActAgent = new ReActAgent(chatClient, PromptTemplate.ReAct, null, toolRegistry, toolExecutor, 8);
        String run = reActAgent.run("成都天气怎么样？然后现在时间是多少？");
        System.out.println(run);
    }

}
