package dingwan.easy.ai.tool.chain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.tool.ToolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ToolChain {

    private final String name;
    private final String description;
    private final List<Map<String, Object>> steps = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ToolChain(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 添加工具链的步骤
     * @param toolName 工具名称
     * @param inputTemplate 输入模板字符串
     * @param outputKey 输出结果的键名，用于后续步骤引用
     */
    public void addStep(String toolName, String inputTemplate, String outputKey) {
        this.steps.add(Map.of(
                "tool_name", toolName,
                "input_template", inputTemplate,
                "output_key", outputKey
        ));
    }

    public String execute(ToolExecutor toolExecutor, String initial_input, Map<String, Object> context) {
        // 执行工具链
        if (context == null) context = new HashMap<>();
        // 设置执行时的初始输入值
        context.put("input", initial_input);

        log.info("\uD83D\uDD17 开始执行工具链: {}", this.name);

        // 维护计数器
        AtomicInteger index = new AtomicInteger();
        Map<String, Object> finalContext = context;
        this.steps.forEach(step -> {
            Object toolName = step.get("tool_name");
            String inputTemplate = String.valueOf(step.get("input_template"));
            Object outputKey = step.get("output_key");

            // 替换模板
            String toolInput;
            try {
                StringSubstitutor sub = new StringSubstitutor(finalContext);
                toolInput = sub.replace(inputTemplate);
            } catch (Exception e) {
                throw new RuntimeException(String.format("❌ 工具链执行失败:模板变量 %s 未找到", e.getMessage()));
            }

            log.info("步骤 {}: 使用 {} 处理 '{}'...", index.incrementAndGet(), this.name, toolInput);

            // 执行工具
            Map<String, Object> toolArgs;
            String execute;
            try {
                toolArgs = objectMapper.readValue(toolInput, new TypeReference<>(){});
                execute = toolExecutor.execute(toolName.toString(), toolArgs).toString();
            } catch (JsonProcessingException e) {
                log.error("工具输入转Map失败");
                throw new RuntimeException(e);
            } catch (Exception e) {
                log.error("工具：{} 执行出错", toolName);
                throw new RuntimeException(e);
            }
            finalContext.put(outputKey.toString(), execute);
            log.info("✅ 步骤 {} 完成，结果长度: {} 字符", index, execute.length());
        });

        // 返回最后一步的结果
        String finalResult = finalContext.get(steps.get(steps.size() - 1).get("output_key")).toString();
        log.info("\uD83C\uDF89 工具链 '{}' 执行完成", this.name);
        return finalResult;
    }

}
