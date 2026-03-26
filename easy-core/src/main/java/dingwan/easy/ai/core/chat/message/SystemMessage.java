package dingwan.easy.ai.core.chat.message;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SystemMessage extends AbstractMessage {

    public static final String MESSAGE_TYPE = MessageType.SYSTEM.name();

    public SystemMessage(MessageType messageType, String content) {
        super(messageType, content);
    }

}
