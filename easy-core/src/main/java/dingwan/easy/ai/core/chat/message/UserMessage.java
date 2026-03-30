package dingwan.easy.ai.core.chat.message;

import dingwan.easy.ai.core.chat.message.content.ContentPart;
import dingwan.easy.ai.core.chat.message.content.ImageContent;
import dingwan.easy.ai.core.chat.message.content.TextContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserMessage extends AbstractMessage {

    /**
     * 纯文本消息
     */
    public UserMessage(String text) {
        super(MessageRole.USER.getValue(), text);
    }

    /**
     * 多模态消息
     */
    public UserMessage(List<ContentPart> contentParts) {
        super(MessageRole.USER.getValue(), contentParts);
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private final List<ContentPart> parts = new ArrayList<>();

        /**
         * 添加文本内容
         */
        public UserBuilder text(String text) {
            parts.add(TextContent.of(text));
            return this;
        }

        /**
         * 添加图片内容
         */
        public UserBuilder image(String url) {
            parts.add(ImageContent.of(url));
            return this;
        }

        /**
         * 添加图片内容（带 detail 参数）
         */
        public UserBuilder image(String url, String detail) {
            parts.add(ImageContent.of(url, detail));
            return this;
        }

        /**
         * 添加自定义内容部分
         */
        public UserBuilder part(ContentPart part) {
            parts.add(part);
            return this;
        }

        /**
         * 添加多个内容部分
         */
        public UserBuilder parts(ContentPart... parts) {
            this.parts.addAll(Arrays.asList(parts));
            return this;
        }

        public UserMessage build() {
            if (parts.isEmpty()) {
                throw new IllegalStateException("UserMessage must have at least one content part");
            }
            return new UserMessage(new ArrayList<>(parts));
        }
    }

}
