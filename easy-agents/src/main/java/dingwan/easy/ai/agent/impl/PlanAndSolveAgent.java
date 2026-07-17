package dingwan.easy.ai.agent.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.agent.config.PromptTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Slf4j
public class PlanAndSolveAgent {

    private final BaseAgent planAgent;
    private final BaseAgent solveAgent;
    private static final ObjectMapper mapper = new ObjectMapper();

    public PlanAndSolveAgent(BaseAgent planAgent, BaseAgent solveAgent) {
        this.planAgent = planAgent;
        this.solveAgent = solveAgent;
    }

    public String run(String inputText, Map<String, Object> kwargs) {
        List<String> history = new ArrayList<>();
        // 构建提示词
        String planPrompt = PromptTemplate.PLANNER_PROMPT
                .replace("{question}", inputText);
        // 将问题规划拆解
        String planResponse = this.planAgent.run(planPrompt, kwargs);
        log.info("planResponse: {}", planResponse);
        // 将计划转成数组
        List<String> planList;
        try {
            planList = mapper.readValue(planResponse, new TypeReference<List<String>>(){});
        } catch (JsonProcessingException e) {
            log.error("将计划解析成数组失败：{}", planResponse);
            throw new RuntimeException(e);
        }
        // 执行计划
        for (int i = 0; i < planList.size(); i++) {
            log.info("正在执行第 {} 步：{}", i + 1, planList.get(i));
            // 构建执行提示词
            String executePrompt = PromptTemplate.EXECUTOR_PROMPT
                    .replace("{question}", inputText)
                    .replace("{plan}", planResponse)
                    .replace("{history}", history.toString())
                    .replace("{current_step}", planList.get(i));
            String executeResponse = this.solveAgent.run(executePrompt, kwargs);
            log.info("executeResponse: {}", executeResponse);
            history.add(String.format("步骤：%s，执行结果：%s\n", planList.get(i), executeResponse));
        }
        return history.get(history.size() - 1);
    }

}
