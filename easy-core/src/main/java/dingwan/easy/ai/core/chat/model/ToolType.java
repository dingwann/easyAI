package dingwan.easy.ai.core.chat.model;

public enum ToolType {

    FUNCTION("function");

    private final String type;

    ToolType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ToolType fromDescription(String description) {
        for (ToolType status : ToolType.values()) {
            if (status.type.equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知状态: " + description);
    }

}
