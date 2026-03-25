package dingwan.easy.ai.core.chat.message;


import java.util.Map;

public class SystemMessage extends AbstractMessage {

    @Override
    public MessageType getMessageType() {
        return null;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Map.of();
    }

    @Override
    public String getText() {
        return "";
    }
}
