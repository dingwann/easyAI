package dingwan.easy.ai.core.chat.message;


public class MessageBuilderFactory extends AbstractMessage {

    public MessageBuilderFactory(String role, String content) {
        super(role, content);
    }

    public static MessageBuilder builder() {
        return new MessageBuilder();
    }

    public static class MessageBuilder {
        private String role;
        private String content;

        public MessageBuilder role(String role) {
            MessageRole messageRole = MessageRole.fromValue(role);
            this.role = messageRole.getValue();
            return this;
        }
        public MessageBuilder role(MessageRole messageRole) {
            this.role = messageRole.getValue();
            return this;
        }
        public MessageBuilder content(String content) {
            this.content = content;
            return this;
        }
        public MessageBuilderFactory build() {
            return new MessageBuilderFactory(this.role, this.content);
        }
    }

}
