package dingwan.easy.ai.core.chat.message;

import java.util.Map;

public class UserMessage extends AbstractMessage {

    public UserMessage(String content) {
        super(content);
    }

    public UserMessage(String content, Map<String, Object> metadata) {
        super(content, metadata);
    }

    @Override
    public String getRole() {
        return "user";
    }
}