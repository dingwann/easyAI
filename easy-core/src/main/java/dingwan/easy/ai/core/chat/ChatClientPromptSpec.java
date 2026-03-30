package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.SystemMessage;
import dingwan.easy.ai.core.chat.message.UserMessage;
import dingwan.easy.ai.core.chat.message.content.ContentPart;
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

    /**
     * 添加多模态用户消息
     */
    public ChatClientPromptSpec user(ContentPart... parts) {
        messages.add(UserMessage.builder().parts(parts).build());
        return this;
    }

    /**
     * 添加带图片的用户消息（便利方法）
     */
    public ChatClientPromptSpec userWithImage(String text, String imageUrl) {
        messages.add(UserMessage.builder()
                .text(text)
                .image(imageUrl)
                .build());
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