# LLM Client Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor LLM client architecture to provide a clean, extensible, and easy-to-use API for chat interactions with multiple provider support.

**Architecture:** ChatClient as facade layer with simple API (call/stream/prompt), ChatModel interface for provider abstraction, and OpenAIChatModel as default implementation. Request/Response/Options clearly separated.

**Tech Stack:** Java 17+, Spring Boot, Lombok, OkHttp, Reactor (Flux for streaming)

---

## File Structure

### New Files
| File | Responsibility |
|------|----------------|
| `chat/ChatModel.java` | Interface defining call/stream contract |
| `chat/model/ChatRequest.java` | Request data (messages + options) |
| `chat/model/ChatResponse.java` | Response data (content + usage + metadata) |
| `chat/model/ChatOptions.java` | Model parameters (temperature, maxTokens, etc.) |
| `chat/model/Usage.java` | Token usage statistics |
| `chat/provider/openai/OpenAIChatModel.java` | OpenAI-compatible implementation |
| `chat/ChatClientPromptSpec.java` | Builder for prompt construction |

### Modified Files
| File | Changes |
|------|---------|
| `chat/ChatClient.java` | Complete rewrite as facade |
| `chat/message/Message.java` | Remove Content interface dependency |
| `chat/message/AbstractMessage.java` | Simplify constructors |
| `chat/message/UserMessage.java` | Remove MessageType parameter |
| `chat/message/SystemMessage.java` | Remove MessageType parameter |
| `chat/message/AssistantMessage.java` | Remove MessageType parameter |
| `config/EasyAiProperties.java` | Add default options field |

### Deleted Files
| File | Reason |
|------|--------|
| `chat/prompt/Prompt.java` | Replaced by ChatRequest |
| `chat/prompt/ChatOptions.java` | Moved to model/ package |
| `chat/ChatClientRequest.java` | Redundant, use ChatRequest |
| `chat/ChatClientResponse.java` | Renamed to ChatResponse |
| `chat/message/MessageType.java` | No longer needed |
| `cntent/Content.java` | Unused interface |

---

## Task 1: Create ChatOptions in model package

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/model/ChatOptions.java`

- [ ] **Step 1: Create ChatOptions class**

```java
package dingwan.easy.ai.core.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatOptions {

    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private List<String> stop;

    public static ChatOptions defaults() {
        return ChatOptions.builder().build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/model/ChatOptions.java
git commit -m "feat: add ChatOptions for model parameters"
```

---

## Task 2: Create Usage class

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/model/Usage.java`

- [ ] **Step 1: Create Usage class**

```java
package dingwan.easy.ai.core.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/model/Usage.java
git commit -m "feat: add Usage for token statistics"
```

---

## Task 3: Create ChatResponse

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/model/ChatResponse.java`

- [ ] **Step 1: Create ChatResponse class**

```java
package dingwan.easy.ai.core.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String content;
    private String model;
    private Usage usage;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public static ChatResponse of(String content) {
        return ChatResponse.builder().content(content).build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/model/ChatResponse.java
git commit -m "feat: add ChatResponse for response data"
```

---

## Task 4: Create ChatRequest

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/model/ChatRequest.java`

- [ ] **Step 1: Create ChatRequest class**

```java
package dingwan.easy.ai.core.chat.model;

import dingwan.easy.ai.core.chat.message.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @Builder.Default
    private List<Message> messages = new ArrayList<>();
    private ChatOptions options;

    public static ChatRequest of(Message... messages) {
        return ChatRequest.builder()
                .messages(Arrays.asList(messages))
                .build();
    }

    public static ChatRequest of(List<Message> messages) {
        return ChatRequest.builder()
                .messages(messages)
                .build();
    }

    public static ChatRequest of(String userText) {
        return ChatRequest.builder()
                .messages(List.of(new UserMessage(userText)))
                .build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/model/ChatRequest.java
git commit -m "feat: add ChatRequest for request data"
```

---

## Task 5: Refactor Message classes

**Files:**
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/chat/message/Message.java`
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/chat/message/AbstractMessage.java`
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/chat/message/UserMessage.java`
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/chat/message/SystemMessage.java`
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/chat/message/AssistantMessage.java`

- [ ] **Step 1: Refactor Message interface**

```java
package dingwan.easy.ai.core.chat.message;

import java.util.Map;

public interface Message {

    String getRole();
    String getText();
    Map<String, Object> getMetadata();
}
```

- [ ] **Step 2: Refactor AbstractMessage**

```java
package dingwan.easy.ai.core.chat.message;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class AbstractMessage implements Message {

    protected final String content;
    protected final Map<String, Object> metadata;

    public AbstractMessage(String content) {
        this.content = content;
        this.metadata = new HashMap<>();
    }

    public AbstractMessage(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    @Override
    public String getText() {
        return this.content;
    }
}
```

- [ ] **Step 3: Refactor UserMessage**

```java
package dingwan.easy.ai.core.chat.message;

public class UserMessage extends AbstractMessage {

    public UserMessage(String content) {
        super(content);
    }

    public UserMessage(String content, Map<String, Object> metadata) {
        super(content, metadata);
    }

    @Override
    public String getRole() {
        return "user";
    }
}
```

- [ ] **Step 4: Refactor SystemMessage**

```java
package dingwan.easy.ai.core.chat.message;

public class SystemMessage extends AbstractMessage {

    public SystemMessage(String content) {
        super(content);
    }

    public SystemMessage(String content, Map<String, Object> metadata) {
        super(content, metadata);
    }

    @Override
    public String getRole() {
        return "system";
    }
}
```

- [ ] **Step 5: Refactor AssistantMessage**

```java
package dingwan.easy.ai.core.chat.message;

public class AssistantMessage extends AbstractMessage {

    public AssistantMessage(String content) {
        super(content);
    }

    public AssistantMessage(String content, Map<String, Object> metadata) {
        super(content, metadata);
    }

    @Override
    public String getRole() {
        return "assistant";
    }
}
```

- [ ] **Step 6: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/message/
git commit -m "refactor: simplify Message classes, remove MessageType dependency"
```

---

## Task 6: Delete obsolete files

**Files:**
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/chat/prompt/Prompt.java`
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/chat/prompt/ChatOptions.java`
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClientRequest.java`
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClientResponse.java`
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/chat/message/MessageType.java`
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/cntent/Content.java`

- [ ] **Step 1: Delete obsolete files**

```bash
rm -f easy-core/src/main/java/dingwan/easy/ai/core/chat/prompt/Prompt.java
rm -f easy-core/src/main/java/dingwan/easy/ai/core/chat/prompt/ChatOptions.java
rm -f easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClientRequest.java
rm -f easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClientResponse.java
rm -f easy-core/src/main/java/dingwan/easy/ai/core/chat/message/MessageType.java
rm -rf easy-core/src/main/java/dingwan/easy/ai/core/cntent/
```

- [ ] **Step 2: Commit**

```bash
git add -A
git commit -m "refactor: remove obsolete Prompt, ChatClientRequest, ChatClientResponse, MessageType"
```

---

## Task 7: Create ChatModel interface

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatModel.java`

- [ ] **Step 1: Create ChatModel interface**

```java
package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatModel {

    ChatResponse call(ChatRequest request);

    Flux<ChatResponse> stream(ChatRequest request);

    String getModelName();
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatModel.java
git commit -m "feat: add ChatModel interface for provider abstraction"
```

---

## Task 8: Update EasyAiProperties

**Files:**
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/config/EasyAiProperties.java`

- [ ] **Step 1: Update EasyAiProperties with default options**

```java
package dingwan.easy.ai.core.config;

import dingwan.easy.ai.core.chat.model.ChatOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
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
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/config/EasyAiProperties.java
git commit -m "feat: add defaultOptions to EasyAiProperties"
```

---

## Task 9: Create OpenAIChatModel

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/provider/openai/OpenAIChatModel.java`

- [ ] **Step 1: Create OpenAIChatModel implementation**

```java
package dingwan.easy.ai.core.chat.provider.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dingwan.easy.ai.core.chat.ChatModel;
import dingwan.easy.ai.core.chat.config.EasyAiProperties;
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
                StringBuilder contentBuilder = new StringBuilder();
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
                                contentBuilder.append(chunk);
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
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/provider/openai/OpenAIChatModel.java
git commit -m "feat: add OpenAIChatModel implementation"
```

---

## Task 10: Create ChatClientPromptSpec

**Files:**
- Create: `easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClientPromptSpec.java`

- [ ] **Step 1: Create ChatClientPromptSpec for builder pattern**

```java
package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.SystemMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class ChatClientPromptSpec {

    private final ChatClient chatClient;
    private final List<Message> messages = new ArrayList<>();
    private ChatOptions options;

    public ChatClientPromptSpec(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatClientPromptSpec system(String content) {
        messages.add(new SystemMessage(content));
        return this;
    }

    public ChatClientPromptSpec user(String content) {
        messages.add(new UserMessage(content));
        return this;
    }

    public ChatClientPromptSpec assistant(String content) {
        messages.add(new AssistantMessage(content));
        return this;
    }

    public ChatClientPromptSpec messages(List<Message> messages) {
        this.messages.addAll(messages);
        return this;
    }

    public ChatClientPromptSpec options(ChatOptions options) {
        this.options = options;
        return this;
    }

    public ChatResponse call() {
        return chatClient.call(buildRequest());
    }

    public Flux<ChatResponse> stream() {
        return chatClient.stream(buildRequest());
    }

    private ChatRequest buildRequest() {
        return ChatRequest.builder()
                .messages(new ArrayList<>(messages))
                .options(options)
                .build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClientPromptSpec.java
git commit -m "feat: add ChatClientPromptSpec for fluent builder pattern"
```

---

## Task 11: Rewrite ChatClient as facade

**Files:**
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClient.java`

- [ ] **Step 1: Rewrite ChatClient**

```java
package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.SystemMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.core.chat.provider.openai.OpenAIChatModel;
import dingwan.easy.ai.core.config.EasyAiProperties;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Getter
public class ChatClient {

    private final ChatModel chatModel;

    public ChatClient(OkHttpClient httpClient, EasyAiProperties properties) {
        this.chatModel = new OpenAIChatModel(httpClient, properties);
    }

    // === Core methods ===

    public ChatResponse call(ChatRequest request) {
        return chatModel.call(request);
    }

    public Flux<ChatResponse> stream(ChatRequest request) {
        return chatModel.stream(request);
    }

    // === Convenience methods ===

    public ChatResponse call(String userText) {
        return call(ChatRequest.of(userText));
    }

    public Flux<ChatResponse> stream(String userText) {
        return stream(ChatRequest.of(userText));
    }

    public ChatResponse call(String systemPrompt, String userText) {
        return call(ChatRequest.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userText)
        ));
    }

    public ChatResponse call(List<Message> messages) {
        return call(ChatRequest.of(messages));
    }

    public Flux<ChatResponse> stream(List<Message> messages) {
        return stream(ChatRequest.of(messages));
    }

    // === Builder pattern ===

    public ChatClientPromptSpec prompt() {
        return new ChatClientPromptSpec(this);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/chat/ChatClient.java
git commit -m "feat: rewrite ChatClient as facade with simple API"
```

---

## Task 12: Update configuration class

**Files:**
- Modify: `easy-core/src/main/java/dingwan/easy/ai/core/config/InitialAutoConfiguration.java`

- [ ] **Step 1: Read current InitialAutoConfiguration**

- [ ] **Step 2: Update if needed to enable ConfigurationProperties**

Ensure `@EnableConfigurationProperties(EasyAiProperties.class)` is present.

- [ ] **Step 3: Commit**

```bash
git add easy-core/src/main/java/dingwan/easy/ai/core/config/InitialAutoConfiguration.java
git commit -m "chore: update configuration for ChatClient"
```

---

## Task 13: Clean up empty prompt directory

**Files:**
- Delete: `easy-core/src/main/java/dingwan/easy/ai/core/chat/prompt/` directory

- [ ] **Step 1: Remove empty prompt directory**

```bash
rmdir easy-core/src/main/java/dingwan/easy/ai/core/chat/prompt/ 2>/dev/null || true
```

- [ ] **Step 2: Commit**

```bash
git add -A
git commit -m "chore: remove empty prompt directory"
```

---

## Task 14: Final verification

- [ ] **Step 1: Build project**

```bash
cd easy-core && mvn clean compile
```

- [ ] **Step 2: Verify imports resolve correctly**

Check that all import statements reference the new package structure.

- [ ] **Step 3: Final commit if any fixes**

```bash
git add -A
git commit -m "fix: resolve any remaining import issues"
```

---

## Summary

| Task | Description |
|------|-------------|
| 1 | Create ChatOptions in model package |
| 2 | Create Usage class |
| 3 | Create ChatResponse |
| 4 | Create ChatRequest |
| 5 | Refactor Message classes |
| 6 | Delete obsolete files |
| 7 | Create ChatModel interface |
| 8 | Update EasyAiProperties |
| 9 | Create OpenAIChatModel |
| 10 | Create ChatClientPromptSpec |
| 11 | Rewrite ChatClient as facade |
| 12 | Update configuration class |
| 13 | Clean up empty directories |
| 14 | Final verification |