package dingwan.easy.ai.agent.impl;

import dingwan.easy.ai.agent.BaseAgent;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.model.ChatOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ExecuteAgent extends BaseAgent {

    public ExecuteAgent(String name, ChatClient chatClient, String systemPrompt, ChatOptions chatOptions) {
        super(name, chatClient, systemPrompt, chatOptions);
    }

    @Override
    public String run(String inputText) {
        return this.run(inputText, null);
    }

    @Override
    public String run(String inputText, Map<String, Object> kwargs) {
        return this.chatClient.prompt()
                .options(ChatOptions.builderByArgs(kwargs))
                .user(inputText)
                .call()
                .getContent();
    }
}
