package cn.intentforge.agent.core;

import cn.intentforge.config.ResolvedRuntimeSelection;
import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.provider.ModelProviderDescriptor;
import cn.intentforge.prompt.model.PromptDefinition;
import cn.intentforge.session.model.Session;
import cn.intentforge.space.ResolvedSpaceProfile;
import cn.intentforge.tool.core.gateway.ToolGateway;
import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.util.List;
import java.util.Objects;

/**
 * Fully resolved runtime context shared across routed agent stages.
 *
 * @param task effective task request
 * @param session target session snapshot
 * @param resolvedSpaceProfile effective space bindings
 * @param runtimeSelection final runtime implementations selected for the run
 * @param prompts resolved prompt definitions available to the route
 * @param models resolved model descriptors available to the route
 * @param modelProviders resolved model provider descriptors available to the route
 * @param tools resolved tool definitions available to the route
 * @param toolGateway selected tool gateway implementation
 * @param toolExecutionContext shared tool execution context
 */
public record ContextPack(
    AgentTask task,
    Session session,
    ResolvedSpaceProfile resolvedSpaceProfile,
    ResolvedRuntimeSelection runtimeSelection,
    List<PromptDefinition> prompts,
    List<ModelDescriptor> models,
    List<ModelProviderDescriptor> modelProviders,
    List<ToolDefinition> tools,
    ToolGateway toolGateway,
    ToolExecutionContext toolExecutionContext
) {
  /**
   * Creates a validated context pack.
   */
  public ContextPack {
    task = Objects.requireNonNull(task, "task must not be null");
    session = Objects.requireNonNull(session, "session must not be null");
    resolvedSpaceProfile = Objects.requireNonNull(resolvedSpaceProfile, "resolvedSpaceProfile must not be null");
    runtimeSelection = Objects.requireNonNull(runtimeSelection, "runtimeSelection must not be null");
    prompts = AgentModelSupport.immutableList(prompts, "prompts");
    models = AgentModelSupport.immutableList(models, "models");
    modelProviders = AgentModelSupport.immutableList(modelProviders, "modelProviders");
    tools = AgentModelSupport.immutableList(tools, "tools");
    toolGateway = Objects.requireNonNull(toolGateway, "toolGateway must not be null");
    toolExecutionContext = Objects.requireNonNull(toolExecutionContext, "toolExecutionContext must not be null");
  }
}
