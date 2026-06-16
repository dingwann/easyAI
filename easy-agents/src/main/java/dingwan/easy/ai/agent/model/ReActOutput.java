package dingwan.easy.ai.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ReActOutput {

    private String thought;

    @JsonProperty("tool_calls")
    private List<ToolCall> toolCalls;

    @JsonProperty("final_answer")
    private String finalAnswer;

}
