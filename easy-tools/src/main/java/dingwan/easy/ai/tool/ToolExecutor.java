package dingwan.easy.ai.tool;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

@Component
public class ToolExecutor {

    private ToolRegistry registry;

    public ToolExecutor(ToolRegistry registry) {
        this.registry = registry;
    }

    public Object execute(String toolName,
                          Map<String, Object> args) throws Exception {

        ToolDefinition tool = registry.get(toolName);

        Method method = tool.getMethod();

        Object[] params = buildArgs(method, args);

        return method.invoke(tool.getTarget(), params);
    }

    private Object[] buildArgs(Method method,
                               Map<String, Object> args) {

        Parameter[] parameters = method.getParameters();

        Object[] result = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            result[i] = args.get(parameters[i].getName());
        }

        return result;
    }
}