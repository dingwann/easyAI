package dingwan.easy.ai.tool;


import dingwan.easy.ai.tool.annotation.AiParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class JsonSchemaUtils {

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
        }

        schema.put("properties", properties);

        return schema;
    }
}