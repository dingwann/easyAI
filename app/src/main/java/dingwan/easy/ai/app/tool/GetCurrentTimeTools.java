package dingwan.easy.ai.app.tool;

import dingwan.easy.ai.tool.annotation.AiTool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class GetCurrentTimeTools {

    @AiTool(description = "获取当前时间，返回格式：yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
