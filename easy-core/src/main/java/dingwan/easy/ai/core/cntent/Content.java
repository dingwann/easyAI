package dingwan.easy.ai.core.cntent;

import java.util.Map;

public interface Content {

    Map<String, Object> getMetadata();
    String getText();

}
