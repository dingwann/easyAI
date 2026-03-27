package dingwan.easy.ai.core.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usage {

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}