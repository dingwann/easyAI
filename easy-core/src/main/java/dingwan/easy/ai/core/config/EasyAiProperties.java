package dingwan.easy.ai.core.config;

import dingwan.easy.ai.core.chat.model.ChatOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "easy.ai.llm")
public class EasyAiProperties {

    private String apiKey;
    private String baseUrl;
    private String model;
    private String provider;
    private ChatOptions defaultOptions;

    public ChatOptions getDefaultOptionsOrDefault() {
        if (defaultOptions == null) {
            defaultOptions = ChatOptions.builder()
                    .model(model)
                    .build();
        }
        return defaultOptions;
    }
}