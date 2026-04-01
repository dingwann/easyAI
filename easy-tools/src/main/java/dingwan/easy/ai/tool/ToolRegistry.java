package dingwan.easy.ai.tool;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ToolRegistry {

    private final Map<String, ToolDefinition> toolMap = new ConcurrentHashMap<>();

    public void register(ToolDefinition tool) {
        toolMap.put(tool.getName(), tool);
    }

    public ToolDefinition get(String name) {
        return toolMap.get(name);
    }

    public Collection<ToolDefinition> getAll() {
        return toolMap.values();
    }

    public int getToolCount() {
        return this.toolMap.size();
    }

    public String getAllToolDesc() {
        Collection<ToolDefinition> values = this.toolMap.values();
        return values.stream()
                .map(entry -> String.format("- %s: %s",
                        entry.getName(),
                        entry.getDescription()))
                .collect(Collectors.joining("\n"));
    }
}