package dingwan.easy.ai.core.chat.message;


import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserMessage extends AbstractMessage {

    public static final String MESSAGE_TYPE = MessageType.USER.name();

    public UserMessage(MessageType messageType, String content) {
        super(messageType, content);
    }

}
