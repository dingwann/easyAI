package dingwan.easy.ai.core.chat.message.content;

import lombok.Getter;

/**
 * 图片内容部分
 */
@Getter
public class ImageContent implements ContentPart {

    private final ImageUrl imageUrl;

    private ImageContent(ImageUrl imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getType() {
        return "image_url";
    }

    public static ImageContent of(String url) {
        return new ImageContent(new ImageUrl(url));
    }

    public static ImageContent of(String url, String detail) {
        return new ImageContent(new ImageUrl(url, detail));
    }

    /**
     * 图片 URL 信息
     */
    @Getter
    public static class ImageUrl {
        private final String url;
        private final String detail;  // "low" | "high" | "auto"，可选

        public ImageUrl(String url) {
            this(url, null);
        }

        public ImageUrl(String url, String detail) {
            this.url = url;
            this.detail = detail;
        }
    }
}
