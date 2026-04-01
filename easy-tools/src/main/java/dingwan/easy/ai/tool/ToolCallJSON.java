package dingwan.easy.ai.tool;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ToolCallJSON {

    private final String name;
    private final Map<String, Object> arguments;

}
