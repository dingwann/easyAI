package dingwan.easy.ai.core.chat.message;

import lombok.Getter;

@Getter
public enum MessageRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    public static MessageRole fromValue(String value) {
        for (MessageRole type : MessageRole.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException(value + " is not a valid message type");
    }
}
