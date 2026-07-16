package dingwan.easy.ai.app.tool;

import dingwan.easy.ai.tool.annotation.AiParam;
import dingwan.easy.ai.tool.annotation.AiTool;
import org.springframework.stereotype.Component;

@Component
public class GetWeatherTools {

    @AiTool(description = "获取指定城市的实时天气信息（含温度、天气状况和风力）")
    public String getWeather(@AiParam("需要查询天气的城市名称，如：北京、上海、广州") String city) {
        return city + "当前28℃ 晴，微风";
    }

}