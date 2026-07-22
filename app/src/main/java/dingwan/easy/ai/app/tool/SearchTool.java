package dingwan.easy.ai.app.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dingwan.easy.ai.tool.annotation.AiParam;
import dingwan.easy.ai.tool.annotation.AiTool;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;


/**
 * 智能混合搜索工具
 * 支持多种搜索引擎后端，智能选择最佳搜索源:
 * 1. 混合模式 (hybrid) - 智能选择 TAVILY 或 SERPAPI
 * 2. Tavily API (tavily) - 专业AI搜索
 * 3. SerpApi (serpapi) - 传统Google搜索
 */
@Slf4j
@Component
public class SearchTool {

    @Value("${tavily_key}")
    private String tavily_key;
    @Value("${serpapi_key}")
    private String serpapi_key;
    @Value("${available_backends}")
    private List<String> available_backends;

    @Autowired
    private OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TAVILY_URL = "https://api.tavily.com/search";
    private static final String SERPAPI_URL = "https://serpapi.com/search";

    @PostConstruct
    public void init() {
        if (tavily_key == null && serpapi_key == null) {
            log.error("tavily_key and serpapi_key is null");
            throw new RuntimeException("tavily_key and serpapi_key is null");
        }
    }

    @AiTool(name = "search_hybrid", description = "搜索工具，智能选择最佳搜索源。")
    public String search_hybrid(@AiParam("查询的问题") String query) {
        // 优先使用Tavily（AI优化的搜索）
        if (this.available_backends.contains("tavily")) {
            try {
                return this.search_tavily(query);
            } catch (Exception e) {
                log.info("⚠ Tavily搜索失败: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        } else if (this.available_backends.contains("serpapi")) {
            try {
                return this.search_serpapi(query);
            } catch (Exception e) {
                log.info("⚠ SerpApi搜索失败: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        // 如果都不可用，提示用户配置API
        return "❌ 没有可用的搜索源，请配置TAVILY_API_KEY或SERPAPI_API_KEY环境变量";
    }

    private String search_tavily(String query) {
        // 构建请求体
        ObjectNode root = objectMapper.createObjectNode();
        root.put("query", query);
        root.put("search_depth", "advanced");
        root.put("max_results", 3);
        root.put("include_answer", true);

        RequestBody body = RequestBody.create(root.toString(), MediaType.parse("application/json"));

        Request httpRequest = new Request.Builder()
                .url(TAVILY_URL)
                .addHeader("Authorization", "Bearer " + tavily_key)
                .post(body)
                .build();

        try (Response response = okHttpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Tavily API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode json = objectMapper.readTree(responseBody);

            StringBuilder result = new StringBuilder();

            // AI直接答案
            if (json.has("answer") && !json.get("answer").isNull()) {
                String answer = json.get("answer").asText();
                if (!answer.isEmpty()) {
                    result.append("💡 AI直接答案:").append(answer).append("\n\n");
                }
            }

            // 相关搜索结果
            result.append("🔗 相关结果:\n");
            JsonNode results = json.path("results");
            if (results.isArray()) {
                int count = Math.min(results.size(), 3);
                for (int i = 0; i < count; i++) {
                    JsonNode item = results.get(i);
                    result.append("[").append(i + 1).append("] ")
                            .append(item.path("title").asText(""))
                            .append("\n");

                    String content = item.path("content").asText("");
                    if (content.length() > 150) {
                        content = content.substring(0, 150) + "...";
                    }
                    result.append(" ").append(content).append("\n\n");
                }
            }

            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("Tavily API call error", e);
        }
    }

    private String search_serpapi(String query) {
        // 构建请求URL（GET方式，参数拼在query string）
        String url = String.format("%s?engine=google&q=%s&api_key=%s&num=3",
                SERPAPI_URL, query, serpapi_key);

        Request httpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = okHttpClient.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("SerpApi API call failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonNode json = objectMapper.readTree(responseBody);

            StringBuilder result = new StringBuilder();
            result.append("🔗 Google搜索结果:\n");

            JsonNode organicResults = json.path("organic_results");
            if (organicResults.isArray()) {
                int count = Math.min(organicResults.size(), 3);
                for (int i = 0; i < count; i++) {
                    JsonNode item = organicResults.get(i);
                    result.append("[").append(i + 1).append("] ")
                            .append(item.path("title").asText(""))
                            .append("\n");
                    result.append(" ").append(item.path("snippet").asText(""))
                            .append("\n\n");
                }
            }

            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException("SerpApi API call error", e);
        }
    }


}
