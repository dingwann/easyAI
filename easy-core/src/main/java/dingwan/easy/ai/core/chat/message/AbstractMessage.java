package dingwan.easy.ai.core.chat.message;


import java.util.HashMap;
import java.util.Map;

public abstract class AbstractMessage implements Message {

    protected final MessageType messageType;
    protected final String content;
    protected final Map<String,Object> metadata;

    public AbstractMessage(MessageType messageType, String content) {
        this.messageType = messageType;
        this.content = content;
        this.metadata = new HashMap<>();
    }

    public AbstractMessage(MessageType messageType, String content, Map<String,Object> metadata) {
        this.messageType = messageType;
        this.content = content;
        this.metadata = metadata;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    @Override
    public MessageType getMessageType() {
        return this.messageType;
    }

    @Override
    public String getText() {
        return this.content;
    }
}
