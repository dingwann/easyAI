package dingwan.easy.ai.core.chat.message;

import dingwan.easy.ai.core.chat.message.content.ContentPart;
import dingwan.easy.ai.core.chat.message.content.TextContent;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractMessage implements Message {

    protected final String role;
    protected final List<ContentPart> contentParts;
    protected final LocalDateTime timestamp = null;
    protected final Map<String, Object> metadata = null;

    /**
     * 纯文本消息构造
     */
    public AbstractMessage(String role, String text) {
        this.role = role;
        this.contentParts = List.of(TextContent.of(text));
    }

    /**
     * 多模态消息构造
     */
    public AbstractMessage(String role, List<ContentPart> contentParts) {
        this.role = role;
        this.contentParts = contentParts;
    }

    @Override
    public String getText() {
        return contentParts.stream()
                .filter(part -> part instanceof TextContent)
                .map(part -> ((TextContent) part).getText())
                .collect(Collectors.joining());
    }

    @Override
    public List<ContentPart> getContentParts() {
        return contentParts;
    }
}
