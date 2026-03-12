package cn.intentforge.config;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpaceConfigurationTest {
  @Test
  void shouldNormalizeSpaceConfiguration() {
    SpaceConfiguration configuration = new SpaceConfiguration(
        " application-alpha ",
        List.of(" skill-a "),
        List.of(" agent-a "),
        List.of(" prompt-a "),
        List.of(" tool-a "),
        List.of(" model-a "),
        List.of(" provider-a "),
        List.of(" memory-a "),
        Map.of(" review.level ", " strict "),
        RuntimeBindings.of(Map.of(RuntimeCapability.PROMPT_MANAGER, " prompt-db ")));

    Assertions.assertEquals("application-alpha", configuration.spaceId());
    Assertions.assertEquals(List.of("skill-a"), configuration.skillIds());
    Assertions.assertEquals("strict", configuration.properties().get("review.level"));
    Assertions.assertEquals("prompt-db", configuration.runtimeBindings().get(RuntimeCapability.PROMPT_MANAGER).orElseThrow());
  }

  @Test
  void shouldRejectBlankSpaceId() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new SpaceConfiguration(
        " ",
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        List.of(),
        Map.of(),
        RuntimeBindings.empty()));
  }
}
