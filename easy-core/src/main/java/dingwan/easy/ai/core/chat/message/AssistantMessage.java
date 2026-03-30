package dingwan.easy.ai.core.chat.message;

public class AssistantMessage extends AbstractMessage {

    public AssistantMessage(String content) {
        super(MessageRole.ASSISTANT.getValue(), content);
    }

    public static AssistantBuilder builder() {
        return new AssistantBuilder();
    }

    public static class AssistantBuilder {
        private String content;

        public AssistantBuilder content(String content) {
            this.content = content;
            return this;
        }
        public AssistantMessage build() {
            return new AssistantMessage(content);
        }
    }

}
