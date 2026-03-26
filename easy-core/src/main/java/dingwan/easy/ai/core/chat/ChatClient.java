package dingwan.easy.ai.core.chat;


import dingwan.easy.ai.core.config.EasyAiProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatClient {

    private final OkHttpClient EasyOkHttpClient;
    private final EasyAiProperties easyAiProperties;
    private static Request request;

    public ChatClient(OkHttpClient easyOkHttpClient, EasyAiProperties easyAiProperties) {
        EasyOkHttpClient = easyOkHttpClient;
        this.easyAiProperties = easyAiProperties;
        ChatClient.request = new Request.Builder()
                .url(easyAiProperties.getBaseUrl() + "/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + easyAiProperties.getApiKey())
                .build();
    }

}
