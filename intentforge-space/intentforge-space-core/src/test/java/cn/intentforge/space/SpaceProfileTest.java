package cn.intentforge.space;

import cn.intentforge.config.SpaceConfiguration;
import cn.intentforge.config.RuntimeBindings;
import cn.intentforge.config.RuntimeCapability;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpaceProfileTest {
  @Test
  void shouldNormalizeMultiValueBindings() {
    SpaceProfile profile = new SpaceProfile(
        List.of(" skill-a "),
        List.of(" agent-a "),
        List.of(" prompt-a "),
        List.of(" tool-a "),
        List.of(" model-a "),
        List.of(" provider-a "),
        List.of(" memory-a "),
        Map.of(" region ", " cn "),
        RuntimeBindings.of(Map.of(RuntimeCapability.PROMPT_MANAGER, " prompt-db ")));

    Assertions.assertEquals(List.of("skill-a"), profile.skillIds());
    Assertions.assertEquals(List.of("agent-a"), profile.agentIds());
    Assertions.assertEquals(List.of("prompt-a"), profile.promptIds());
    Assertions.assertEquals(List.of("tool-a"), profile.toolIds());
    Assertions.assertEquals(List.of("model-a"), profile.modelIds());
    Assertions.assertEquals(List.of("provider-a"), profile.modelProviderIds());
    Assertions.assertEquals(List.of("memory-a"), profile.memoryIds());
    Assertions.assertEquals(Map.of("region", "cn"), profile.config());
    Assertions.assertEquals("prompt-db", profile.runtimeBindings().get(RuntimeCapability.PROMPT_MANAGER).orElseThrow());
  }

  @Test
  void shouldCreateProfileFromSpaceConfiguration() {
    SpaceProfile profile = SpaceProfile.fromConfiguration(new SpaceConfiguration(
        "application-alpha",
        List.of("skill-a"),
        List.of("agent-a"),
        List.of("prompt-a"),
        List.of("tool-a"),
        List.of("model-a"),
        List.of("provider-a"),
        List.of("memory-a"),
        Map.of("review.level", "strict"),
        RuntimeBindings.of(Map.of(RuntimeCapability.PROMPT_MANAGER, "prompt-db"))));

    Assertions.assertEquals(List.of("skill-a"), profile.skillIds());
    Assertions.assertEquals(List.of("agent-a"), profile.agentIds());
    Assertions.assertEquals(List.of("prompt-a"), profile.promptIds());
    Assertions.assertEquals("prompt-db", profile.runtimeBindings().get(RuntimeCapability.PROMPT_MANAGER).orElseThrow());
  }

  @Test
  void shouldRejectBlankResourceIdentifier() {
    IllegalArgumentException exception = Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SpaceProfile(
            List.of(" "),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null));

    Assertions.assertTrue(exception.getMessage().contains("skillIds item"));
  }
}
