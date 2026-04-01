package dingwan.easy.ai.tool;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;

@Getter
public class ToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, Object> parameterSchema;
    private final Method method;
    private final Object target;

    public ToolDefinition(String name,
                          String description,
                          Map<String, Object> parameterSchema,
                          Method method,
                          Object target) {
        this.name = name;
        this.description = description;
        this.parameterSchema = parameterSchema;
        this.method = method;
        this.target = target;
    }

}