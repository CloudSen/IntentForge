package cn.intentforge.governance.agent;

import cn.intentforge.config.ResolvedRuntimeSelection;
import cn.intentforge.model.provider.registry.ModelProviderRegistry;
import cn.intentforge.model.registry.ModelManager;
import cn.intentforge.prompt.registry.PromptManager;
import cn.intentforge.tool.core.gateway.ToolGateway;
import java.util.Objects;

/**
 * Groups the runtime components selected for one run together with their observable selection metadata.
 *
 * @param runtimeSelection selected implementation descriptors
 * @param promptManager prompt manager chosen for the run
 * @param modelManager model manager chosen for the run
 * @param modelProviderRegistry model-provider registry chosen for the run
 * @param toolGateway tool gateway chosen for the run
 */
public record ResolvedAgentRuntime(
    ResolvedRuntimeSelection runtimeSelection,
    PromptManager promptManager,
    ModelManager modelManager,
    ModelProviderRegistry modelProviderRegistry,
    ToolGateway toolGateway
) {
  /**
   * Creates a validated resolved runtime container.
   */
  public ResolvedAgentRuntime {
    runtimeSelection = Objects.requireNonNull(runtimeSelection, "runtimeSelection must not be null");
    promptManager = Objects.requireNonNull(promptManager, "promptManager must not be null");
    modelManager = Objects.requireNonNull(modelManager, "modelManager must not be null");
    modelProviderRegistry = Objects.requireNonNull(modelProviderRegistry, "modelProviderRegistry must not be null");
    toolGateway = Objects.requireNonNull(toolGateway, "toolGateway must not be null");
  }
}
