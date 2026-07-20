package dingwan.easy.ai.core.chat.model;

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
public class FunctionObject {

    private String name;

    private String description;

    private ParametersForTool parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParametersForTool {
        private String type = "object";
        private Map<String, Object> properties;
        private List<String> required;
        @Builder.Default
        private boolean additionalProperties = false;
    }
}
