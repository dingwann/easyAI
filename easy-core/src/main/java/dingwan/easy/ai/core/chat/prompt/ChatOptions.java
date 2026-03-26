package dingwan.easy.ai.core.chat.prompt;

import dingwan.easy.ai.core.chat.message.Message;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatOptions {

    private final String model;
    private final Double temperature;
    private final int top_p;
    private final int top_k;
    private final Long max_tokens;
    private final boolean stream;

}
