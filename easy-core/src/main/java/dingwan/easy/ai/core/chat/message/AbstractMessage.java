package dingwan.easy.ai.core.chat.message;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class AbstractMessage implements Message {

    protected final String content;
    protected final Map<String, Object> metadata;

    public AbstractMessage(String content) {
        this.content = content;
        this.metadata = new HashMap<>();
    }

    public AbstractMessage(String content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    @Override
    public String getText() {
        return this.content;
    }
}