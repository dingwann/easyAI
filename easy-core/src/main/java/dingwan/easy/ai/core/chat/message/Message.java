package dingwan.easy.ai.core.chat.message;

import dingwan.easy.ai.core.chat.message.content.ContentPart;

import java.util.List;

public interface Message {

    String getRole();

    /**
     * 获取文本内容（便利方法，用于纯文本消息或拼接所有文本部分）
     */
    String getText();

    /**
     * 获取内容部分列表（支持多模态）
     * 默认返回单文本部分，子类可覆盖
     */
    default List<ContentPart> getContentParts() {
        return List.of();
    }

    /**
     * 是否包含多个内容部分（多模态）
     */
    default boolean hasMultipleParts() {
        return getContentParts().size() > 1;
    }
}
