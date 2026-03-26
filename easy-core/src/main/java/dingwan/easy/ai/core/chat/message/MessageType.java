package dingwan.easy.ai.core.chat.message;

import lombok.Getter;

import java.util.Arrays;

/**
 * 消息类型枚举（兼容 OpenAI / 通用 LLM 协议）
 */
@Getter
public enum MessageType {

    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    TOOL("tool");

    /**
     * 对外协议值（用于 JSON / API 传输）
     */
    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    /**
     * 根据字符串解析枚举（忽略大小写）
     */
    public static MessageType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MessageType: " + value));
    }

    /**
     * 是否是用户输入
     */
    public boolean isUser() {
        return this == USER;
    }

    /**
     * 是否是系统指令
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }

    /**
     * 是否是模型输出
     */
    public boolean isAssistant() {
        return this == ASSISTANT;
    }

    /**
     * 是否是工具调用相关
     */
    public boolean isTool() {
        return this == TOOL;
    }

    @Override
    public String toString() {
        return value;
    }
}