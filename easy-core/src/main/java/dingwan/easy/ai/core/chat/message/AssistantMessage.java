package dingwan.easy.ai.core.chat.message;

import dingwan.easy.ai.core.chat.model.ToolCall;
import lombok.Getter;

import java.util.List;

public class AssistantMessage extends AbstractMessage {

    @Getter
    private List<ToolCall> tool_calls;

    public AssistantMessage(String content) {
        super(MessageRole.ASSISTANT.getValue(), content);
    }
    public AssistantMessage(String content, List<ToolCall> tool_calls) {
        super(MessageRole.ASSISTANT.getValue(), content);
        this.tool_calls = tool_calls;
    }

    public static AssistantBuilder builder() {
        return new AssistantBuilder();
    }

    public static class AssistantBuilder {
        private String content;
        private List<ToolCall> tool_calls;

        public AssistantBuilder content(String content) {
            this.content = content;
            return this;
        }
        public AssistantBuilder toolcalls(List<ToolCall> toolcalls) {
            this.tool_calls = toolcalls;
            return this;
        }
        public AssistantMessage build() {
            return new AssistantMessage(content, tool_calls);
        }
    }

}
