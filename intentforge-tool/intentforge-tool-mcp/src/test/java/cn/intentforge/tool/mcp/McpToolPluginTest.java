package cn.intentforge.tool.mcp;

import cn.intentforge.tool.core.registry.InMemoryToolRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class McpToolPluginTest {
  @Test
  void shouldLoadPluginWithoutRemoteTools() {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    registry.loadPlugins();
    Assertions.assertTrue(registry.list().stream().noneMatch(item -> item.definition().id().startsWith("intentforge.mcp.")));
  }
}
