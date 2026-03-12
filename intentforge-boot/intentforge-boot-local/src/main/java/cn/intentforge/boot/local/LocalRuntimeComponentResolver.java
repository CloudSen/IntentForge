package cn.intentforge.boot.local;

import cn.intentforge.config.ResolvedRuntimeSelection;
import cn.intentforge.config.RuntimeBindings;
import cn.intentforge.config.RuntimeCapability;
import cn.intentforge.config.RuntimeCatalog;
import cn.intentforge.config.RuntimeImplementationDescriptor;
import cn.intentforge.governance.agent.AgentRuntimeResolver;
import cn.intentforge.governance.agent.ResolvedAgentRuntime;
import cn.intentforge.space.ResolvedSpaceProfile;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves per-run runtime components from the discovered runtime catalog and the effective space bindings.
 */
public final class LocalRuntimeComponentResolver implements AgentRuntimeResolver {
  private static final RuntimeCapability[] REQUIRED_RUN_CAPABILITIES = {
      RuntimeCapability.PROMPT_MANAGER,
      RuntimeCapability.MODEL_MANAGER,
      RuntimeCapability.MODEL_PROVIDER_REGISTRY,
      RuntimeCapability.TOOL_REGISTRY
  };

  private final RuntimeCatalog runtimeCatalog;
  private final LocalRuntimeComponentRegistry runtimeComponents;
  private final ResolvedRuntimeSelection infrastructureSelection;

  /**
   * Creates a resolver from the discovered runtime catalog and component instances.
   *
   * @param runtimeCatalog discovered runtime catalog
   * @param runtimeComponents discovered component instances
   * @param infrastructureSelection fixed bootstrap-scoped selections such as the session manager
   */
  public LocalRuntimeComponentResolver(
      RuntimeCatalog runtimeCatalog,
      LocalRuntimeComponentRegistry runtimeComponents,
      ResolvedRuntimeSelection infrastructureSelection
  ) {
    this.runtimeCatalog = Objects.requireNonNull(runtimeCatalog, "runtimeCatalog must not be null");
    this.runtimeComponents = Objects.requireNonNull(runtimeComponents, "runtimeComponents must not be null");
    this.infrastructureSelection = Objects.requireNonNull(infrastructureSelection, "infrastructureSelection must not be null");
  }

  /**
   * Returns the discovered runtime components registry.
   *
   * @return discovered runtime components
   */
  public LocalRuntimeComponentRegistry runtimeComponents() {
    return runtimeComponents;
  }

  /**
   * Resolves runtime components for one run.
   *
   * @param resolvedSpaceProfile effective space profile for the current run
   * @return selected runtime components and observable selection metadata
   */
  @Override
  public ResolvedAgentRuntime resolve(ResolvedSpaceProfile resolvedSpaceProfile) {
    ResolvedSpaceProfile nonNullResolvedSpaceProfile =
        Objects.requireNonNull(resolvedSpaceProfile, "resolvedSpaceProfile must not be null");
    ResolvedRuntimeSelection selection = infrastructureSelection.overlay(resolveRunSelection(nonNullResolvedSpaceProfile.runtimeBindings()));
    String promptRuntimeId = requireSelection(selection, RuntimeCapability.PROMPT_MANAGER).id();
    String modelRuntimeId = requireSelection(selection, RuntimeCapability.MODEL_MANAGER).id();
    String providerRuntimeId = requireSelection(selection, RuntimeCapability.MODEL_PROVIDER_REGISTRY).id();
    String toolRuntimeId = requireSelection(selection, RuntimeCapability.TOOL_REGISTRY).id();
    return new ResolvedAgentRuntime(
        selection,
        runtimeComponents.promptManager(promptRuntimeId),
        runtimeComponents.modelManager(modelRuntimeId),
        runtimeComponents.modelProviderRegistry(providerRuntimeId),
        runtimeComponents.toolGateway(toolRuntimeId));
  }

  private ResolvedRuntimeSelection resolveRunSelection(RuntimeBindings runtimeBindings) {
    RuntimeBindings nonNullBindings = Objects.requireNonNull(runtimeBindings, "runtimeBindings must not be null");
    Map<RuntimeCapability, RuntimeImplementationDescriptor> selected = new EnumMap<>(RuntimeCapability.class);
    for (Map.Entry<RuntimeCapability, String> entry : nonNullBindings.values().entrySet()) {
      RuntimeCapability capability = entry.getKey();
      RuntimeImplementationDescriptor descriptor = runtimeCatalog.find(capability, entry.getValue())
          .orElseThrow(() -> new IllegalArgumentException(
              "runtime implementation not found for capability " + capability + ": " + entry.getValue()));
      RuntimeImplementationDescriptor infrastructureDescriptor = infrastructureSelection.get(capability).orElse(null);
      if (infrastructureDescriptor != null && !infrastructureDescriptor.id().equals(descriptor.id())) {
        throw new IllegalArgumentException(
            "runtime capability " + capability + " is bootstrap scoped and must use " + infrastructureDescriptor.id());
      }
      selected.put(capability, descriptor);
    }
    for (RuntimeCapability capability : REQUIRED_RUN_CAPABILITIES) {
      selected.computeIfAbsent(capability, this::resolveDefaultSelection);
    }
    return new ResolvedRuntimeSelection(selected);
  }

  private RuntimeImplementationDescriptor resolveDefaultSelection(RuntimeCapability capability) {
    return runtimeCatalog.defaultImplementation(capability)
        .orElseThrow(() -> new IllegalStateException(
            "no unique runtime implementation available for capability " + capability));
  }

  private static RuntimeImplementationDescriptor requireSelection(
      ResolvedRuntimeSelection selection,
      RuntimeCapability capability
  ) {
    return selection.get(capability)
        .orElseThrow(() -> new IllegalStateException("runtime selection missing capability " + capability));
  }
}
