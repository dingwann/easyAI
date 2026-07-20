package dingwan.easy.ai.core.chat.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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
    private List<ToolRequest> tools;

    public static ChatOptions defaults() {
        return ChatOptions.builder().build();
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ChatOptions builderByArgs(Map<String, Object> args) {
        return OBJECT_MAPPER.convertValue(args, ChatOptions.class);
    }
}