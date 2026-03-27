# LLM客户端重新设计

## 概述

重新设计LLM客户端架构，解决现有代码职责混乱、类重复、扩展性差的问题，打造一个全面且简易好用的LLM客户端。

## 现有问题

1. **Prompt类职责混乱** - 同时持有`Content`、`message`、`messages`、`chatOptions`四个字段，构造方式多达8种
2. **ChatOptions和ChatClientRequest重复** - 字段几乎完全相同，存在冗余
3. **ChatClient功能不完整** - 只有构造函数，没有实际调用逻辑
4. **Message子类设计问题** - 如`UserMessage`需要传入`MessageType.USER`，但类名已经表明类型
5. **命名不一致** - 包名`cntent`拼写错误，字段`Content`首字母大写等

## 设计目标

- 支持完整功能：单轮/多轮对话、流式输出、工具调用(Function Calling)、多模态
- 支持多提供商：统一接口 + 适配器模式，默认提供OpenAI兼容格式实现
- 易用性：简洁方法 + Builder模式

## 架构设计

```
┌─────────────────────────────────────────────────────────┐
│                      ChatClient                         │
│  (门面层，提供简洁API: call(), stream(), prompt())       │
└─────────────────────────┬───────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────┐
│                   ChatModel (接口)                       │
│  - call(ChatRequest): ChatResponse                      │
│  - stream(ChatRequest): Flux<ChatResponse>               │
└─────────────────────────┬───────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
   ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
   │OpenAIChatModel│ │AnthropicModel│ │ 其他提供商... │
   │  (默认实现)    │ │   (扩展)      │ │              │
   └──────────────┘ └──────────────┘ └──────────────┘
```

## 文件结构

```
chat/
├── ChatClient.java           # 门面，提供易用API
├── ChatModel.java            # 接口，定义调用契约
├── model/
│   ├── ChatRequest.java      # 请求数据（包含messages + options）
│   ├── ChatResponse.java     # 响应数据（content + usage + metadata）
│   └── ChatOptions.java      # 模型参数（temperature, maxTokens等）
├── message/
│   ├── Message.java          # 消息接口
│   ├── AbstractMessage.java  # 抽象基类
│   ├── SystemMessage.java    # 系统消息
│   ├── UserMessage.java      # 用户消息
│   └── AssistantMessage.java  # 助手消息
└── provider/
    └── openai/
        └── OpenAIChatModel.java  # OpenAI兼容实现
```

## 核心类设计

### ChatOptions

模型参数配置，只保留与模型调用相关的参数。

```java
public class ChatOptions {
    private String model;           // 模型名称
    private Double temperature;     // 温度
    private Integer maxTokens;      // 最大token
    private Double topP;            // top_p
    private List<String> stop;      // 停止词
    // 使用Builder模式
}
```

### ChatRequest

请求封装，包含消息列表和可选参数。

```java
public class ChatRequest {
    private List<Message> messages;  // 消息列表
    private ChatOptions options;      // 可选参数

    // 静态工厂方法
    public static ChatRequest of(Message... messages);
    public static ChatRequest of(List<Message> messages);

    // Builder模式
    public static Builder builder();
}
```

### ChatResponse

响应封装，包含完整响应信息。

```java
public class ChatResponse {
    private String content;           // 响应内容
    private String model;             // 使用的模型
    private Usage usage;              // token使用量
    private Map<String, Object> metadata;  // 元数据

    public static class Usage {
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }
}
```

### Message子类简化

不再需要传MessageType，类型由类名决定。

```java
// 之前
new UserMessage(MessageType.USER, "你好")

// 之后
new UserMessage("你好")  // 类型由类名决定
```

## 接口设计

### ChatModel接口

```java
public interface ChatModel {
    ChatResponse call(ChatRequest request);
    Flux<ChatResponse> stream(ChatRequest request);
    String getModelName();
}
```

### ChatClient API

门面层提供易用API。

```java
public class ChatClient {
    private final ChatModel chatModel;

    // 基础方法
    public ChatResponse call(ChatRequest request);
    public Flux<ChatResponse> stream(ChatRequest request);

    // 便捷方法 - 快速单轮对话
    public ChatResponse call(String userText);
    public Flux<ChatResponse> stream(String userText);

    // Builder模式构建请求
    public ChatClientPromptSpec prompt();  // 返回Builder

    // 带系统提示
    public ChatResponse call(String systemPrompt, String userText);
}
```

## 使用示例

### 最简单用法

```java
chatClient.call("你好");
```

### 流式输出

```java
chatClient.stream("讲个故事").subscribe(chunk -> {
    System.out.print(chunk.getContent());
});
```

### 完整控制

```java
ChatResponse response = chatClient.prompt()
    .system("你是一个助手")
    .user("你好")
    .options(ChatOptions.builder().temperature(0.7).build())
    .call();
```

### 多轮对话

```java
chatClient.call(ChatRequest.of(
    new SystemMessage("你是助手"),
    new UserMessage("你好"),
    new AssistantMessage("你好！有什么可以帮助你的？"),
    new UserMessage("今天天气怎么样")
));
```

## 变更清单

### 删除的类

- `Prompt.java` - 职责混乱，由`ChatRequest`替代
- `ChatClientRequest.java` - 与`ChatOptions`重复，删除

### 重构的类

- `ChatOptions.java` - 移动到`model/`包，移除messages字段
- `ChatClientResponse.java` - 重命名为`ChatResponse`，增加`Usage`内部类
- `Message.java` - 简化，移除对`Content`接口的依赖
- `AbstractMessage.java` - 简化构造函数
- `UserMessage.java` - 简化，不再需要传MessageType
- `SystemMessage.java` - 同上
- `AssistantMessage.java` - 同上

### 新增的类

- `ChatModel.java` - 接口，定义调用契约
- `ChatRequest.java` - 请求数据封装
- `OpenAIChatModel.java` - OpenAI兼容格式实现
- `ChatClientPromptSpec.java` - Builder模式的中间类

### 修复

- 包名`cntent` → `content`
- 字段命名规范化（首字母小写）

## 扩展性

新增提供商只需实现`ChatModel`接口：

```java
public class AnthropicChatModel implements ChatModel {
    // 实现call和stream方法
}
```

然后通过配置或代码注入到`ChatClient`即可。