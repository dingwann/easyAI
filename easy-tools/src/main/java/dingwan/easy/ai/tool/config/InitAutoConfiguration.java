package dingwan.easy.ai.tool.config;

import dingwan.easy.ai.tool.ToolAnnotationScanner;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ApplicationContext.class)
public class InitAutoConfiguration {

    @Autowired ApplicationContext applicationContext;

    @Bean
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    @Bean
    @ConditionalOnBean(ToolRegistry.class)
    public ToolExecutor toolExecutor() {
        return new ToolExecutor(toolRegistry());
    }

    @Bean
    public ToolAnnotationScanner toolAnnotationScanner() {
        return new ToolAnnotationScanner(toolRegistry(), applicationContext);
    }

}
