package cn.intentforge.config;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResolvedRuntimeSelectionTest {
  @Test
  void shouldExposeSelectedImplementationIdsAndOverlaySelections() {
    ResolvedRuntimeSelection base = new ResolvedRuntimeSelection(Map.of(
        RuntimeCapability.SESSION_MANAGER,
        new RuntimeImplementationDescriptor(
            "session-default",
            RuntimeCapability.SESSION_MANAGER,
            "Session",
            "example.Session",
            Map.of())));
    ResolvedRuntimeSelection overrides = new ResolvedRuntimeSelection(Map.of(
        RuntimeCapability.PROMPT_MANAGER,
        new RuntimeImplementationDescriptor(
            "prompt-db",
            RuntimeCapability.PROMPT_MANAGER,
            "Prompt",
            "example.Prompt",
            Map.of())));

    ResolvedRuntimeSelection merged = base.overlay(overrides);

    Assertions.assertEquals("session-default", merged.get(RuntimeCapability.SESSION_MANAGER).orElseThrow().id());
    Assertions.assertEquals("prompt-db", merged.get(RuntimeCapability.PROMPT_MANAGER).orElseThrow().id());
    Assertions.assertEquals(
        Map.of(
            "SESSION_MANAGER", "session-default",
            "PROMPT_MANAGER", "prompt-db"),
        merged.selectedImplementationIds());
  }
}
