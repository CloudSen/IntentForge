package cn.intentforge.common.plugin;

import java.util.Optional;

public final class PluginCompatibilityValidator {
  private PluginCompatibilityValidator() {
  }

  public static Optional<String> validate(
      PluginMetadata metadata,
      String expectedApiVersion,
      String runtimeVersion
  ) {
    if (metadata.apiVersion() == null) {
      return Optional.of("Missing plugin.apiVersion");
    }
    if (!metadata.apiVersion().equals(expectedApiVersion)) {
      return Optional.of(
          "Incompatible plugin.apiVersion: expected " + expectedApiVersion + " but was " + metadata.apiVersion());
    }
    if (metadata.intentforgeVersion() != null
        && !"*".equals(metadata.intentforgeVersion())
        && !metadata.intentforgeVersion().equals(runtimeVersion)) {
      return Optional.of(
          "Incompatible plugin.intentforgeVersion: expected " + runtimeVersion + " but was "
              + metadata.intentforgeVersion());
    }
    return Optional.empty();
  }
}
