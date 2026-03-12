package cn.intentforge.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable runtime implementation bindings keyed by {@link RuntimeCapability}.
 *
 * @param values configured runtime selections
 */
public record RuntimeBindings(
    Map<RuntimeCapability, String> values
) {
  /**
   * Creates an empty runtime binding set.
   *
   * @return empty binding set
   */
  public static RuntimeBindings empty() {
    return new RuntimeBindings(Map.of());
  }

  /**
   * Creates one immutable binding set from raw values.
   *
   * @param values raw binding values
   * @return normalized binding set
   */
  public static RuntimeBindings of(Map<RuntimeCapability, String> values) {
    return new RuntimeBindings(values);
  }

  /**
   * Creates a validated immutable binding set.
   *
   * @param values configured runtime selections
   */
  public RuntimeBindings {
    Map<RuntimeCapability, String> source = Objects.requireNonNull(values, "values must not be null");
    Map<RuntimeCapability, String> normalized = new LinkedHashMap<>();
    for (Map.Entry<RuntimeCapability, String> entry : source.entrySet()) {
      Objects.requireNonNull(entry, "values entry must not be null");
      RuntimeCapability capability = Objects.requireNonNull(entry.getKey(), "runtime binding capability must not be null");
      String implementationId = normalize(entry.getValue(), "runtime binding value");
      normalized.put(capability, implementationId);
    }
    values = Map.copyOf(normalized);
  }

  /**
   * Looks up one configured implementation identifier.
   *
   * @param capability runtime capability
   * @return configured implementation identifier when present
   */
  public Optional<String> get(RuntimeCapability capability) {
    return Optional.ofNullable(values.get(Objects.requireNonNull(capability, "capability must not be null")));
  }

  /**
   * Returns whether no runtime selections are configured.
   *
   * @return {@code true} when empty
   */
  public boolean isEmpty() {
    return values.isEmpty();
  }

  /**
   * Overlays the provided bindings on top of the current set.
   *
   * @param overrides overriding bindings
   * @return merged binding set
   */
  public RuntimeBindings overlay(RuntimeBindings overrides) {
    RuntimeBindings nonNullOverrides = Objects.requireNonNull(overrides, "overrides must not be null");
    if (nonNullOverrides.values.isEmpty()) {
      return this;
    }
    Map<RuntimeCapability, String> merged = new LinkedHashMap<>(values);
    merged.putAll(nonNullOverrides.values);
    return new RuntimeBindings(merged);
  }

  private static String normalize(String value, String fieldName) {
    String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return normalized;
  }
}
