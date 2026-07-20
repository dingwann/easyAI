package dingwan.easy.ai.tool;


import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.core.chat.model.FunctionObject;
import dingwan.easy.ai.core.chat.model.ToolRequest;
import dingwan.easy.ai.tool.annotation.AiParam;
import dingwan.easy.ai.tool.annotation.AiTool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonSchemaUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * schema --> 如下示例
     * {
     *     type: object,
     *     properties: {
     *         argName1: {
     *             type: String,
     *             description: 这是参数1注解描述
     *         },
     *         argName2: {
     *                   type: int,
     *                   description: 这是参数2注解描述
     *               },
     *     }
     * }
     * @param method 方法
     * @return 参数schema
     */
    public static Map<String, Object> generateSchema(Method method) {

        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();
        List<String> required = new ArrayList<>();

        // 获取方法的所有参数
        Parameter[] parameters = method.getParameters();

        for (Parameter param : parameters) {

            Map<String, Object> paramSchema = new HashMap<>();
            // 获取参数类型
            Class<?> type = param.getType();

            if (type == String.class) {
                paramSchema.put("type", "string");
            } else if (type == Integer.class || type == int.class) {
                paramSchema.put("type", "integer");
            } else if (type == Long.class || type == long.class) {
                paramSchema.put("type", "long");
            } else if (type == Double.class || type == double.class) {
                paramSchema.put("type", "double");
            } else if (type == Boolean.class || type == boolean.class) {
                paramSchema.put("type", "boolean");
            } else {
                paramSchema.put("type", "object");
            }

            if (param.isAnnotationPresent(AiParam.class)) {
                AiParam p = param.getAnnotation(AiParam.class);
                paramSchema.put("description", p.value());
            }

            properties.put(param.getName(), paramSchema);
            required.add(param.getName());
        }

        schema.put("properties", properties);
        schema.put("required", required);
        schema.put("additionalProperties", false);

        return schema;
    }

    /**
     * 将 ToolDefinition 转换为 FunctionObject（用于原生 Function Calling）
     * @param toolDef 工具定义
     * @return FunctionObject
     */
    public static FunctionObject toFunctionObject(ToolDefinition toolDef) {
        Map<String, Object> schema = toolDef.getParameterSchema();
        FunctionObject.ParametersForTool parameters = OBJECT_MAPPER.convertValue(
                schema, FunctionObject.ParametersForTool.class);

        return FunctionObject.builder()
                .name(toolDef.getName())
                .description(toolDef.getDescription())
                .parameters(parameters)
                .build();
    }

    /**
     * 从单个 @AiTool 方法直接生成 FunctionObject
     * @param method 标注了 @AiTool 的方法
     * @return FunctionObject
     */
    public static FunctionObject toFunctionObject(Method method) {
        AiTool ann = method.getAnnotation(AiTool.class);
        String name = ann.name().isEmpty() ? method.getName() : ann.name();
        String desc = ann.description();
        Map<String, Object> schema = generateSchema(method);
        FunctionObject.ParametersForTool parameters = OBJECT_MAPPER.convertValue(
                schema, FunctionObject.ParametersForTool.class);

        return FunctionObject.builder()
                .name(name)
                .description(desc)
                .parameters(parameters)
                .build();
    }

    /**
     * 扫描工具类中的所有 @AiTool 方法，构建 ToolRequest 列表
     * 支持父类方法扫描，可直接传入 .class 使用
     *
     * @param toolClass 包含 @AiTool 注解方法的类
     * @return ToolRequest 列表
     */
    public static List<ToolRequest> buildToolRequests(Class<?> toolClass) {
        return buildToolRequestsFromClass(toolClass);
    }

    /**
     * 扫描多个工具类中的所有 @AiTool 方法，构建 ToolRequest 列表
     * 支持父类方法扫描
     *
     * @param first      第一个工具类
     * @param rest       其余工具类
     * @return ToolRequest 列表
     */
    public static List<ToolRequest> buildToolRequests(Class<?> first, Class<?>... rest) {
        List<ToolRequest> tools = new ArrayList<>(buildToolRequestsFromClass(first));
        for (Class<?> clazz : rest) {
            tools.addAll(buildToolRequestsFromClass(clazz));
        }
        return tools;
    }

    private static List<ToolRequest> buildToolRequestsFromClass(Class<?> toolClass) {
        List<ToolRequest> tools = new ArrayList<>();

        // 扫描当前类声明的方法
        for (Method method : toolClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AiTool.class)) {
                FunctionObject func = toFunctionObject(method);
                tools.add(ToolRequest.builder().function(func).build());
            }
        }

        // 递归扫描父类（处理 CGLIB 代理等场景）
        Class<?> superClass = toolClass.getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AiTool.class)) {
                    FunctionObject func = toFunctionObject(method);
                    tools.add(ToolRequest.builder().function(func).build());
                }
            }
            superClass = superClass.getSuperclass();
        }

        return tools;
    }
}