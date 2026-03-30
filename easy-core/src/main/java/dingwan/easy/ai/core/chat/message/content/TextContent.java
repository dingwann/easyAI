package dingwan.easy.ai.core.chat.message.content;

import lombok.Getter;

/**
 * 文本内容部分
 */
@Getter
public class TextContent implements ContentPart {

    private final String text;

    private TextContent(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return "text";
    }

    public static TextContent of(String text) {
        return new TextContent(text);
    }
}
