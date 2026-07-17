package dingwan.easy.ai.app;

import dingwan.easy.ai.agent.impl.ExecuteAgent;
import dingwan.easy.ai.agent.impl.PlanAgent;
import dingwan.easy.ai.agent.impl.PlanAndSolveAgent;
import dingwan.easy.ai.agent.impl.ReflectionAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PlanAndSolveAgentTest {

    @Autowired ChatClient chatClient;

    @Test
    public void test() {
        PlanAgent planAgent = new PlanAgent("PlanAgent", chatClient, null, null);
        ExecuteAgent executeAgent = new ExecuteAgent("ExecuteAgent", chatClient, null, null);
        PlanAndSolveAgent planAndSolveAgent = new PlanAndSolveAgent(planAgent, executeAgent);
        String run = planAndSolveAgent.run("一个水果店周一卖出了15个苹果。周二卖出的苹果数量是周一的两倍。周三卖出的数量比周二少了5个。请问这三天总共卖出了多少个苹果？", null);
        System.out.println(run);
    }

}
