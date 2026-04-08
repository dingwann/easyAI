package dingwan.easy.ai.agent.config;

import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.agent.impl.SimpleAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.config.CoreAutoConfiguration;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@AutoConfigureAfter({CoreAutoConfiguration.class})
public class InitialAutoConfiguration {

    @Autowired ChatClient chatClient;
    @Autowired ToolExecutor toolExecutor;
    @Autowired ToolRegistry toolRegistry;

    // @Bean
    public BaseAgent simpleAgent() {
        return new SimpleAgent("simpleAgent", chatClient, null, null, toolRegistry, toolExecutor, true, 6);
    }

}
