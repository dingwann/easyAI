package dingwan.easy.ai.agent.impl;

import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 基于基类重写简单对话Agent
 */
@Slf4j
public class SimpleAgent extends BaseAgent {

    private final Map<String, Object> toolRegistry = null;
    private final boolean toolEnabled = true;

    public SimpleAgent(String name, ChatClient chatClient, String systemPrompt, ChatOptions chatOptions) {
        super(name, chatClient, systemPrompt, chatOptions);
        log.info("✅ {} 初始化完成，工具调用: {}", this.name, this.toolEnabled ? "启用" : "禁用");
    }

    @Override
    public String run(String inputText, Map<String, Object> kwargs) {
        return "";
    }

}
