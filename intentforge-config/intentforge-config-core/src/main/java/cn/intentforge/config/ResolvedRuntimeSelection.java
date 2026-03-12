package cn.intentforge.config;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable snapshot of the runtime implementations selected for one run.
 *
 * @param implementations selected implementations keyed by capability
 */
public record ResolvedRuntimeSelection(
    Map<RuntimeCapability, RuntimeImplementationDescriptor> implementations
) {
  /**
   * Creates an empty selection.
   *
   * @return empty selection
   */
  public static ResolvedRuntimeSelection empty() {
    return new ResolvedRuntimeSelection(Map.of());
  }

  /**
   * Creates a validated immutable selection.
   *
   * @param implementations selected implementations keyed by capability
   */
  public ResolvedRuntimeSelection {
    Map<RuntimeCapability, RuntimeImplementationDescriptor> source =
        Objects.requireNonNull(implementations, "implementations must not be null");
    Map<RuntimeCapability, RuntimeImplementationDescriptor> normalized = new EnumMap<>(RuntimeCapability.class);
    for (Map.Entry<RuntimeCapability, RuntimeImplementationDescriptor> entry : source.entrySet()) {
      Objects.requireNonNull(entry, "implementations entry must not be null");
      RuntimeCapability capability = Objects.requireNonNull(entry.getKey(), "runtime capability must not be null");
      RuntimeImplementationDescriptor descriptor =
          Objects.requireNonNull(entry.getValue(), "runtime implementation descriptor must not be null");
      if (descriptor.capability() != capability) {
        throw new IllegalArgumentException(
            "runtime implementation capability mismatch: expected " + capability + " but got " + descriptor.capability());
      }
      normalized.put(capability, descriptor);
    }
    implementations = Map.copyOf(new LinkedHashMap<>(normalized));
  }

  /**
   * Finds the selected implementation for the provided capability.
   *
   * @param capability target capability
   * @return selected implementation when present
   */
  public Optional<RuntimeImplementationDescriptor> get(RuntimeCapability capability) {
    return Optional.ofNullable(implementations.get(Objects.requireNonNull(capability, "capability must not be null")));
  }

  /**
   * Returns the selected implementation identifiers keyed by capability name.
   *
   * @return immutable capability-to-identifier map
   */
  public Map<String, String> selectedImplementationIds() {
    Map<String, String> selected = new LinkedHashMap<>();
    for (Map.Entry<RuntimeCapability, RuntimeImplementationDescriptor> entry : implementations.entrySet()) {
      selected.put(entry.getKey().name(), entry.getValue().id());
    }
    return Map.copyOf(selected);
  }

  /**
   * Overlays the provided selection on top of the current one.
   *
   * @param overrides overriding selected implementations
   * @return merged selection
   */
  public ResolvedRuntimeSelection overlay(ResolvedRuntimeSelection overrides) {
    ResolvedRuntimeSelection nonNullOverrides = Objects.requireNonNull(overrides, "overrides must not be null");
    if (nonNullOverrides.implementations.isEmpty()) {
      return this;
    }
    Map<RuntimeCapability, RuntimeImplementationDescriptor> merged = new EnumMap<>(RuntimeCapability.class);
    merged.putAll(implementations);
    merged.putAll(nonNullOverrides.implementations);
    return new ResolvedRuntimeSelection(merged);
  }
}
