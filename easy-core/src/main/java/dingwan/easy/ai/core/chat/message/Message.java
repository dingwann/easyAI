package dingwan.easy.ai.core.chat.message;

import java.util.Map;

public interface Message {

    String getRole();
    String getText();
    Map<String, Object> getMetadata();

}