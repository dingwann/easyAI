package dingwan.easy.ai.core.chat.message;

public class SystemMessage extends AbstractMessage {

    public SystemMessage(String content) {
        super(content);
    }

    public SystemMessage(String content, Map<String, Object> metadata) {
        super(content, metadata);
    }

    @Override
    public String getRole() {
        return "system";
    }
}