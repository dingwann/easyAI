package dingwan.easy.ai.core.chat.prompt;

import dingwan.easy.ai.core.chat.message.Message;
import lombok.Builder;

import java.util.List;

@Builder
public class Prompt {

    private String Content;
    private Message message;
    private List<Message> messages;
    private ChatOptions chatOptions;

    public Prompt(String Content) {
        this.Content = Content;
    }

    public Prompt(String Content, ChatOptions chatOptions) {
        this.Content = Content;
        this.chatOptions = chatOptions;
    }

    public Prompt(Message message) {
        this.message = message;
    }

    public Prompt(Message message, ChatOptions chatOptions) {
        this.message = message;
        this.chatOptions = chatOptions;
    }

    public Prompt(List<Message> messages) {
        this.messages = messages;
    }

    public Prompt(List<Message> messages, ChatOptions chatOptions) {
        this.messages = messages;
        this.chatOptions = chatOptions;
    }

    public Prompt(Message... messages) {
        this.messages = List.of(messages);
    }

    public Prompt(ChatOptions chatOptions, Message... messages) {
        this.messages = List.of(messages);
        this.chatOptions = chatOptions;
    }

}
