package dingwan.easy.ai.core.chat.message;

public class ToolMessage extends AbstractMessage {
    private final String tool_call_id;

    public ToolMessage(String content, String toolCallId) {
        super(MessageRole.TOOL.getValue(), content);
        this.tool_call_id = toolCallId;
    }

    public static ToolBuilder builder() {
        return new ToolBuilder();
    }

    public static class ToolBuilder {
        private String content;
        private String tool_call_id;

        public ToolBuilder content(String content) {
            this.content = content;
            return this;
        }

        public ToolBuilder toolCallId(String toolCallId) {
            this.tool_call_id = toolCallId;
            return this;
        }

        public ToolMessage build() {
            return new ToolMessage(content, tool_call_id);
        }
    }

}
