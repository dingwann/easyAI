package dingwan.easy.ai.core.chat.model;

import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.message.UserMessage;
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