package dingwan.easy.ai.core.config;

import dingwan.easy.ai.core.chat.model.ChatOptions;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Slf4j
@ConfigurationProperties(prefix = "easy.ai.llm")
public class EasyAiProperties {

    private String apiKey;
    private String baseUrl;
    private String model;
    private String provider;
    private ChatOptions defaultOptions;

    @PostConstruct
    public void init() {
        if (baseUrl == null) {
            throw new IllegalArgumentException("easy.ai.llm.base-url 未配置");
        }
        if (model == null) {
            throw new IllegalArgumentException("easy.ai.llm.model 未配置");
        }

        log.info("LLM配置加载完成: baseUrl={}, model={}, provider={}\ndefaultOptions={}", baseUrl, model, provider, defaultOptions);
    }

    public ChatOptions getDefaultOptionsOrDefault() {
        if (defaultOptions == null) {
            defaultOptions = ChatOptions.builder()
                    .model(model)
                    .build();
        }
        return defaultOptions;
    }
}