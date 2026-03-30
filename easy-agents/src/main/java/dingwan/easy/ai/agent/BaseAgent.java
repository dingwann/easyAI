package dingwan.easy.ai.agent;

import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.Message;
import dingwan.easy.ai.core.chat.model.ChatOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Agent基类
 */
public abstract class BaseAgent {

    protected final String name;
    protected final ChatClient chatClient;
    protected final String systemPrompt;
    protected final ChatOptions chatOptions;
    protected final List<Message> messages = new ArrayList<>();

    protected BaseAgent(String name, ChatClient chatClient, String systemPrompt, ChatOptions chatOptions) {
        this.name = name;
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
        this.chatOptions = chatOptions;
    }

    /**
     * 运行入口
     * @param inputText 输入问题
     * @param kwargs 参数
     * @return 结果
     */
    abstract String run(String inputText, Map<String, Object> kwargs);

    /**
     * 添加消息到历史记录
     * @param message 消息
     */
    protected void addMessage(Message message) {
        this.messages.add(message);
    }

    /**
     * 清空历史记录
     */
    protected void clearMessage() {
        this.messages.clear();
    }

    /**
     * 获取历史记录
     */
    protected List<Message> getMessage() {
        return this.messages.stream().toList();
    }

    @Override
    public String toString() {
        return String.format("Agent[name='%s', provider='%s']", name, "OpenAI");
    }
}
