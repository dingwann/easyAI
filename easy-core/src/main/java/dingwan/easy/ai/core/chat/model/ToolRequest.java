package dingwan.easy.ai.core.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolRequest {

    @Builder.Default
    private String type = ToolType.FUNCTION.getType();

    private FunctionObject function;

}
