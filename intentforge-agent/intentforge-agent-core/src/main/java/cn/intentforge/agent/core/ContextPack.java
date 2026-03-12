package cn.intentforge.agent.core;

import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.provider.ModelProviderDescriptor;
import cn.intentforge.prompt.model.PromptDefinition;
import cn.intentforge.session.model.Session;
import cn.intentforge.space.ResolvedSpaceProfile;
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
 * @param prompts resolved prompt definitions available to the route
 * @param models resolved model descriptors available to the route
 * @param modelProviders resolved model provider descriptors available to the route
 * @param tools resolved tool definitions available to the route
 * @param toolExecutionContext shared tool execution context
 */
public record ContextPack(
    AgentTask task,
    Session session,
    ResolvedSpaceProfile resolvedSpaceProfile,
    List<PromptDefinition> prompts,
    List<ModelDescriptor> models,
    List<ModelProviderDescriptor> modelProviders,
    List<ToolDefinition> tools,
    ToolExecutionContext toolExecutionContext
) {
  /**
   * Creates a validated context pack.
   */
  public ContextPack {
    task = Objects.requireNonNull(task, "task must not be null");
    session = Objects.requireNonNull(session, "session must not be null");
    resolvedSpaceProfile = Objects.requireNonNull(resolvedSpaceProfile, "resolvedSpaceProfile must not be null");
    prompts = AgentModelSupport.immutableList(prompts, "prompts");
    models = AgentModelSupport.immutableList(models, "models");
    modelProviders = AgentModelSupport.immutableList(modelProviders, "modelProviders");
    tools = AgentModelSupport.immutableList(tools, "tools");
    toolExecutionContext = Objects.requireNonNull(toolExecutionContext, "toolExecutionContext must not be null");
  }
}
