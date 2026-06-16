package dingwan.easy.ai.agent.model;

import lombok.Data;

import java.util.Map;

@Data
public class ToolCall {

    private String name;

    private Map<String, Object> arguments;
}