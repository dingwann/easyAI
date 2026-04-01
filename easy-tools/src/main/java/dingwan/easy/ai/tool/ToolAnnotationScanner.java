package dingwan.easy.ai.tool;

import dingwan.easy.ai.tool.annotation.AiTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Component
public class ToolAnnotationScanner implements CommandLineRunner {

    private final ToolRegistry registry;
    private final ApplicationContext applicationContext;

    public ToolAnnotationScanner(ToolRegistry registry, ApplicationContext applicationContext) {
        this.registry = registry;
        this.applicationContext = applicationContext;
    }

    /**
     * 扫描 Spring 容器中所有带有 @AiTool 注解的方法
     */
    private void scanAllTools() {
        // 获取所有 Bean 名称
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            scanSingleBean(bean);
        }
    }

    /**
     * 扫描单个 Bean 中的 @AiTool 方法（复用你原有的逻辑）
     */
    private void scanSingleBean(Object bean) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(AiTool.class)) {
                registerTool(bean, method);
            }
        }

        // getDeclaredMethods() 只获取当前类声明的方法
        // 如果父类也有 @AiTool，需要递归扫描父类
        Class<?> superClass = bean.getClass().getSuperclass();
        while (superClass != null && superClass != Object.class) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(AiTool.class)) {
                    registerTool(bean, method);
                }
            }
            superClass = superClass.getSuperclass();
        }
    }

    /**
     * 提取注册逻辑，避免重复
     */
    private void registerTool(Object bean, Method method) {
        AiTool toolAnn = method.getAnnotation(AiTool.class);

        String name = toolAnn.name().isEmpty() ? method.getName() : toolAnn.name();
        String desc = toolAnn.description();
        Map<String, Object> schema = JsonSchemaUtils.generateSchema(method);

        ToolDefinition tool = new ToolDefinition(name, desc, schema, method, bean);
        registry.register(tool);

        log.info("📦 注册工具: [{}]", name);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("🔧 开始扫描 @AiTool 注解的工具...");

        // 扫描所有 Bean（或指定包下的 Bean）
        scanAllTools();

        log.info("✅ 工具扫描完成，已注册 {} 个工具", registry.getToolCount());
    }
}