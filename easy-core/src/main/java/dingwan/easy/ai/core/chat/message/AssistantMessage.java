package dingwan.easy.ai.core.chat.message;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AssistantMessage extends AbstractMessage {

    public static final String MESSAGE_TYPE = MessageType.ASSISTANT.name();

    public AssistantMessage(MessageType messageType, String content) {
        super(messageType, content);
    }

}
