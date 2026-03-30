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

    public ChatClient(OkHttpClient easyOkHttpClient, EasyAiProperties properties) {
        this.chatModel = new OpenAIChatModel(easyOkHttpClient, properties);
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