package dingwan.easy.ai.tool.chain;

import dingwan.easy.ai.tool.ToolExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 工具链管理工具
 */
@Slf4j
@Data
public class ToolChainManager {

    private final ToolExecutor toolExecutor;
    private final Map<String, ToolChain> toolChains;

    public ToolChainManager(ToolExecutor toolExecutor) {
        this.toolExecutor = toolExecutor;
        this.toolChains = new HashMap<>();
    }

    public void registerChain(ToolChain toolChain) {
        this.toolChains.put(toolChain.getName(), toolChain);
        log.info("✅ 工具链 '{}' 已注册", toolChain.getName());
    }

    public String executeChain(String chainName, String inputData, Map<String, Object> context) {
        if (context == null) context = new HashMap<>();
        if (!toolChains.containsKey(chainName)) {
            return String.format("❌ 工具链 '%s' 不存在", chainName);
        }
        ToolChain toolChain = this.toolChains.get(chainName);
        return toolChain.execute(toolExecutor, inputData, context);
    }

    public Set<String> listChains() {
        return toolChains.keySet();
    }

}
