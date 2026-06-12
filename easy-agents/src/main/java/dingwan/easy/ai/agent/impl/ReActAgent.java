package dingwan.easy.ai.agent.impl;

import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import dingwan.easy.ai.tool.ToolExecutor;
import dingwan.easy.ai.tool.ToolRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ReActAgent extends BaseAgent {

    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final int maxToolIteration;

    public ReActAgent(ChatClient chatClient, String systemPrompt, ChatOptions chatOptions, ToolRegistry toolRegistry, ToolExecutor toolExecutor, int maxToolIteration) {
        super("ReAct", chatClient, systemPrompt, chatOptions);
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.maxToolIteration = maxToolIteration;
    }

    @Override
    public String run(String inputText) {
        return "";
    }

    @Override
    public String run(String inputText, Map<String, Object> kwargs) {
        return "";
    }

}
