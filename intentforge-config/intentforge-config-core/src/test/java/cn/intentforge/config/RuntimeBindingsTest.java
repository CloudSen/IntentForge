package cn.intentforge.config;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RuntimeBindingsTest {
  @Test
  void shouldNormalizeAndOverlayRuntimeSelections() {
    RuntimeBindings base = RuntimeBindings.of(Map.of(
        RuntimeCapability.PROMPT_MANAGER, " prompt-db ",
        RuntimeCapability.MEMORY_STORE, " memory-sql "));
    RuntimeBindings overrides = RuntimeBindings.of(Map.of(
        RuntimeCapability.MEMORY_STORE, "memory-graph",
        RuntimeCapability.TOOL_REGISTRY, "tool-default"));

    RuntimeBindings resolved = base.overlay(overrides);

    Assertions.assertEquals("prompt-db", resolved.get(RuntimeCapability.PROMPT_MANAGER).orElseThrow());
    Assertions.assertEquals("memory-graph", resolved.get(RuntimeCapability.MEMORY_STORE).orElseThrow());
    Assertions.assertEquals("tool-default", resolved.get(RuntimeCapability.TOOL_REGISTRY).orElseThrow());
  }

  @Test
  void shouldRejectBlankRuntimeSelection() {
    IllegalArgumentException exception = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> RuntimeBindings.of(Map.of(RuntimeCapability.MODEL_MANAGER, " ")));

    Assertions.assertTrue(exception.getMessage().contains("runtime binding value"));
  }
}
