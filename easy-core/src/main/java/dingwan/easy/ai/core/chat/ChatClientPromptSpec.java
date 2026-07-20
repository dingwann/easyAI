package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.message.*;
import dingwan.easy.ai.core.chat.message.content.ContentPart;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.core.chat.model.ToolRequest;
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

    public ChatClientPromptSpec assistant(AssistantMessage assistantMessage) {
        messages.add(assistantMessage);
        return this;
    }

    public ChatClientPromptSpec tool(String tool, String toolCallId) {
        messages.add(new ToolMessage(tool, toolCallId));
        return this;
    }

    public ChatClientPromptSpec tool(ToolMessage toolMessage) {
        messages.add(toolMessage);
        return this;
    }

    public ChatClientPromptSpec tool(List<ToolMessage> toolMessages) {
        messages.addAll(toolMessages);
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

    /**
     * 设置工具列表（原生 Function Calling）
     * 会合并到已有 options 中
     */
    public ChatClientPromptSpec tools(List<ToolRequest> tools) {
        if (this.options == null) {
            this.options = ChatOptions.builder().tools(tools).build();
        } else {
            this.options.setTools(tools);
        }
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