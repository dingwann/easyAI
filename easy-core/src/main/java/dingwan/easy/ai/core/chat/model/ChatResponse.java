package dingwan.easy.ai.core.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
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
    private List<ToolCall> tool_calls;

    public static ChatResponse of(String content) {
        return ChatResponse.builder().content(content).build();
    }
}