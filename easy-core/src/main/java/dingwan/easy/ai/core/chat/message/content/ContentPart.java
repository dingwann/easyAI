package dingwan.easy.ai.core.chat.message.content;

/**
 * 消息内容部分的接口，支持多模态消息。
 * 实现：TextContent、ImageContent 等
 */
public interface ContentPart {

    /**
     * 获取内容类型
     * @return "text" | "image_url" | ...
     */
    String getType();
}
