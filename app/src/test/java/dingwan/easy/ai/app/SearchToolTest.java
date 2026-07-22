package dingwan.easy.ai.app;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dingwan.easy.ai.app.tool.SearchTool;
import dingwan.easy.ai.core.chat.ChatClient;
import dingwan.easy.ai.core.chat.message.AssistantMessage;
import dingwan.easy.ai.core.chat.message.ToolMessage;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import dingwan.easy.ai.core.chat.model.ToolCall;
import dingwan.easy.ai.tool.JsonSchemaUtils;
import dingwan.easy.ai.tool.ToolExecutor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class SearchToolTest {

    @Autowired ChatClient chatClient;
    @Autowired ToolExecutor toolExecutor;
    @Autowired ObjectMapper objectMapper;
    @Autowired SearchTool searchTool;

    @Test
    void testSearchTool() throws Exception{
        String string = searchTool.search_hybrid("人工智能的最新发展");
        System.out.println(string);
    }

    @Test
    void testFunctionCalling() throws Exception {
        ChatResponse chatResponse = chatClient.prompt()
                .user("人工智能的最新发展")
                .tools(JsonSchemaUtils.buildToolRequests(SearchTool.class))
                .call();
        System.out.println("chatResponse = " + chatResponse);
        // 工具消息
        List<ToolMessage> toolMessageList = new ArrayList<>();
        // 执行工具
        List<ToolCall> toolCalls = chatResponse.getTool_calls();
        for (ToolCall toolCall : toolCalls) {
            System.out.println("toolCall = " + toolCall);
            String name = toolCall.getFunction().getName();
            String arguments = toolCall.getFunction().getArguments();
            Map<String, Object> map = objectMapper.readValue(
                    arguments,
                    new TypeReference<Map<String, Object>>() {}
            );
            Object execute = toolExecutor.execute(name, map);
            System.out.println("system tool execute = " + execute);
            // 构建工具消息
            ToolMessage toolMessage = ToolMessage.builder()
                    .toolCallId(toolCall.getId())
                    .content(execute.toString())
                    .build();
            toolMessageList.add(toolMessage);
        }
        // 构建助手消息
        AssistantMessage assistantMessage = AssistantMessage.builder()
                .toolcalls(toolCalls)
                .build();
        // 调用
        ChatResponse response = chatClient.prompt()
                .user("人工智能的最新发展")
                .assistant(assistantMessage)
                .tool(toolMessageList)
                .call();

        System.out.println("final response = " + response.getContent());
    }

}
