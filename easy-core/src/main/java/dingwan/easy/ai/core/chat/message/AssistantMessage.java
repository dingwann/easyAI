package dingwan.easy.ai.core.chat.message;

public class AssistantMessage extends AbstractMessage {

    public AssistantMessage(String content) {
        super(content);
    }

    public AssistantMessage(String content, Map<String, Object> metadata) {
        super(content, metadata);
    }

    @Override
    public String getRole() {
        return "assistant";
    }
}