package dingwan.easy.ai.tool.config;

import dingwan.easy.ai.tool.ToolAnnotationScanner;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ApplicationContext.class)
public class ToolAutoConfiguration {

    @Bean
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    @Bean
    public ToolExecutor toolExecutor(ToolRegistry toolRegistry) {
        return new ToolExecutor(toolRegistry);
    }

    @Bean
    public ToolAnnotationScanner toolAnnotationScanner(ToolRegistry toolRegistry, ApplicationContext applicationContext) {
        return new ToolAnnotationScanner(toolRegistry, applicationContext);
    }

}
