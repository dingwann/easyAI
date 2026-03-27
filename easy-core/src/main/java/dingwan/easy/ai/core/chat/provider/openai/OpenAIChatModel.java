package dingwan.easy.ai.core.chat.provider.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dingwan.easy.ai.core.chat.ChatModel;
import dingwan.easy.ai.core.config.EasyAiProperties;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.core.chat.model.Usage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class OpenAIChatModel implements ChatModel {

    private final OkHttpClient httpClient;
    private final EasyAiProperties properties;
    private final ObjectMapper objectMapper;
    private final String endpoint;

    public OpenAIChatModel(OkHttpClient httpClient, EasyAiProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.endpoint = properties.getBaseUrl() + "/v1/chat/completions";
    }

    @Override
    public ChatResponse call(ChatRequest request) {
        RequestBody body = buildRequestBody(request, false);
        Request httpRequest = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + properties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API call failed: " + response.code());
            }
            return parseResponse(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException("API call error", e);
        }
    }

    @Override
    public Flux<ChatResponse> stream(ChatRequest request) {
        return Flux.create(sink -> {
            RequestBody body = buildRequestBody(request, true);
            Request httpRequest = new Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer " + properties.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try {
                Response response = httpClient.newCall(httpRequest).execute();
                if (!response.isSuccessful()) {
                    sink.error(new RuntimeException("API call failed: " + response.code()));
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body().byteStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JsonNode node = objectMapper.readTree(data);
                            JsonNode delta = node.path("choices").path(0).path("delta").path("content");
                            if (delta.isTextual()) {
                                String chunk = delta.asText();
                                sink.next(ChatResponse.of(chunk));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
                sink.complete();
            } catch (IOException e) {
                sink.error(e);
            }
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    @Override
    public String getModelName() {
        return properties.getModel();
    }

    private RequestBody buildRequestBody(ChatRequest request, boolean stream) {
        ObjectNode root = objectMapper.createObjectNode();
        ChatOptions options = mergeOptions(request.getOptions());

        root.put("model", options.getModel() != null ? options.getModel() : properties.getModel());
        root.put("stream", stream);

        if (options.getTemperature() != null) {
            root.put("temperature", options.getTemperature());
        }
        if (options.getMaxTokens() != null) {
            root.put("max_tokens", options.getMaxTokens());
        }
        if (options.getTopP() != null) {
            root.put("top_p", options.getTopP());
        }
        if (options.getStop() != null && !options.getStop().isEmpty()) {
            ArrayNode stopArray = root.putArray("stop");
            options.getStop().forEach(stopArray::add);
        }

        ArrayNode messagesArray = root.putArray("messages");
        for (Message msg : request.getMessages()) {
            ObjectNode msgNode = messagesArray.addObject();
            msgNode.put("role", msg.getRole());
            msgNode.put("content", msg.getText());
        }

        return RequestBody.create(root.toString(), MediaType.parse("application/json"));
    }

    private ChatOptions mergeOptions(ChatOptions requestOptions) {
        ChatOptions defaults = properties.getDefaultOptionsOrDefault();
        if (requestOptions == null) {
            return defaults;
        }
        return ChatOptions.builder()
                .model(requestOptions.getModel() != null ? requestOptions.getModel() : defaults.getModel())
                .temperature(requestOptions.getTemperature() != null ? requestOptions.getTemperature() : defaults.getTemperature())
                .maxTokens(requestOptions.getMaxTokens() != null ? requestOptions.getMaxTokens() : defaults.getMaxTokens())
                .topP(requestOptions.getTopP() != null ? requestOptions.getTopP() : defaults.getTopP())
                .stop(requestOptions.getStop() != null ? requestOptions.getStop() : defaults.getStop())
                .build();
    }

    private ChatResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            String model = root.path("model").asText();

            JsonNode usageNode = root.path("usage");
            Usage usage = Usage.builder()
                    .promptTokens(usageNode.path("prompt_tokens").asInt())
                    .completionTokens(usageNode.path("completion_tokens").asInt())
                    .totalTokens(usageNode.path("total_tokens").asInt())
                    .build();

            return ChatResponse.builder()
                    .content(content)
                    .model(model)
                    .usage(usage)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}