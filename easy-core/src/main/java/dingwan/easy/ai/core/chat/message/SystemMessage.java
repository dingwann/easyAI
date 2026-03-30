package dingwan.easy.ai.core.chat.message;

public class SystemMessage extends AbstractMessage {

    public SystemMessage(String content) {
        super(MessageRole.SYSTEM.getValue(), content);
    }

    public static SystemBuilder builder() {
        return new SystemBuilder();
    }

    public static class SystemBuilder {
        private String content;

        public SystemBuilder content(String content) {
            this.content = content;
            return this;
        }
        public SystemMessage build() {
            return new SystemMessage(content);
        }
    }

}
