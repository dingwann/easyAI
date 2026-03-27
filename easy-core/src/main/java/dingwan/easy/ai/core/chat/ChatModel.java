package dingwan.easy.ai.core.chat;

import dingwan.easy.ai.core.chat.model.ChatRequest;
import dingwan.easy.ai.core.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

public interface ChatModel {

    ChatResponse call(ChatRequest request);

    Flux<ChatResponse> stream(ChatRequest request);

    String getModelName();
}