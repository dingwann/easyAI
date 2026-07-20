package dingwan.easy.ai.core.chat.provider.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dingwan.easy.ai.core.chat.ChatModel;
import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.ToolMessage;
import dingwan.easy.ai.core.chat.model.*;
import dingwan.easy.ai.core.config.EasyAiProperties;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.message.content.ContentPart;
import dingwan.easy.ai.core.chat.message.content.ImageContent;
import dingwan.easy.ai.core.chat.message.content.TextContent;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
        String baseUrl = properties.getBaseUrl();
        if (!baseUrl.endsWith("/v1"))
            baseUrl = baseUrl + "/v1";
        this.endpoint = baseUrl + "/chat/completions";
    }

    @Override
    public ChatResponse call(ChatRequest request) {
        log.info("ChatRequest: {}", request);
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

        // 序列化 tools 数组（原生 Function Calling）
        if (options.getTools() != null && !options.getTools().isEmpty()) {
            ArrayNode toolsArray = root.putArray("tools");
            for (ToolRequest tool : options.getTools()) {
                ObjectNode toolNode = toolsArray.addObject();
                toolNode.put("type", tool.getType());
                if (tool.getFunction() != null) {
                    ObjectNode funcNode = toolNode.putObject("function");
                    funcNode.put("name", tool.getFunction().getName());
                    funcNode.put("description", tool.getFunction().getDescription());
                    if (tool.getFunction().getParameters() != null) {
                        funcNode.putPOJO("parameters", tool.getFunction().getParameters());
                    }
                }
            }
        }

        ArrayNode messagesArray = root.putArray("messages");
        for (Message msg : request.getMessages()) {
            ObjectNode msgNode = messagesArray.addObject();
            msgNode.put("role", msg.getRole());

            // 处理多模态消息
            if (msg instanceof UserMessage && msg.hasMultipleParts()) {
                ArrayNode contentArray = msgNode.putArray("content");
                for (ContentPart part : msg.getContentParts()) {
                    ObjectNode partNode = contentArray.addObject();
                    partNode.put("type", part.getType());
                    if (part instanceof TextContent tc) {
                        partNode.put("text", tc.getText());
                    } else if (part instanceof ImageContent ic) {
                        ObjectNode imageUrlNode = partNode.putObject("image_url");
                        imageUrlNode.put("url", ic.getImageUrl().getUrl());
                        if (ic.getImageUrl().getDetail() != null) {
                            imageUrlNode.put("detail", ic.getImageUrl().getDetail());
                        }
                    }
                }
            } else if (msg instanceof AssistantMessage assistantMessage && assistantMessage.getTool_calls() != null) {
                try {
                    // 工具消息
                    JsonNode toolCallsNode = objectMapper.valueToTree(assistantMessage.getTool_calls());
                    msgNode.put("tool_calls", toolCallsNode);
                } catch (Exception e) {
                    log.error("工具消息转换JSON出错");
                    throw new RuntimeException(e);
                }
            } else if (msg instanceof ToolMessage tm && tm.getTool_call_id() != null) {
                msgNode.put("content", msg.getText());
                msgNode.put("tool_call_id", tm.getTool_call_id());
            } else {
                msgNode.put("content", msg.getText());
            }
        }

        log.info("Request Body: {}", root);

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
                .tools(requestOptions.getTools() != null ? requestOptions.getTools() : defaults.getTools())
                .build();
    }

    private ChatResponse parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            log.info("Response Body Root: {}", root);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            String model = root.path("model").asText();

            JsonNode usageNode = root.path("usage");
            Usage usage = Usage.builder()
                    .promptTokens(usageNode.path("prompt_tokens").asInt())
                    .completionTokens(usageNode.path("completion_tokens").asInt())
                    .totalTokens(usageNode.path("total_tokens").asInt())
                    .build();

            // 工具
            // 1. 取出 tool_calls 节点
            JsonNode toolCallsNode = root.path("choices").path(0).path("message").path("tool_calls");

            // 2. 判断是否为数组且有内容
            List<ToolCall> toolCalls = new ArrayList<>();
            if (toolCallsNode.isArray() && !toolCallsNode.isEmpty()) {
                toolCalls = objectMapper.convertValue(
                        toolCallsNode,
                        new TypeReference<List<ToolCall>>() {
                        }
                );
            }

            return ChatResponse.builder()
                    .content(content)
                    .model(model)
                    .usage(usage)
                    .tool_calls(toolCalls)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response", e);
        }
    }
}