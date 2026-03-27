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