package cn.intentforge.tool.validation;

import cn.intentforge.tool.core.registry.InMemoryToolRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationToolPluginTest {
  @Test
  void shouldLoadValidationPlugin() {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    registry.loadPlugins();
    Assertions.assertTrue(registry.find(ValidationToolPlugin.TOOL_BUILD).isPresent());
    Assertions.assertTrue(registry.find(ValidationToolPlugin.TOOL_TEST).isPresent());
    Assertions.assertTrue(registry.find(ValidationToolPlugin.TOOL_LINT).isPresent());
  }
}
