package cn.intentforge.governance.agent;

import cn.intentforge.agent.core.AgentDescriptor;
import cn.intentforge.agent.core.AgentExecutionException;
import cn.intentforge.agent.core.AgentExecutionState;
import cn.intentforge.agent.core.AgentExecutor;
import cn.intentforge.agent.core.AgentGateway;
import cn.intentforge.agent.core.AgentRoute;
import cn.intentforge.agent.core.AgentRunResult;
import cn.intentforge.agent.core.AgentStepResult;
import cn.intentforge.agent.core.AgentTask;
import cn.intentforge.agent.core.ContextPack;
import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.provider.ModelProvider;
import cn.intentforge.model.provider.ModelProviderDescriptor;
import cn.intentforge.model.provider.registry.ModelProviderRegistry;
import cn.intentforge.model.registry.ModelManager;
import cn.intentforge.prompt.model.PromptDefinition;
import cn.intentforge.prompt.registry.PromptManager;
import cn.intentforge.session.model.Session;
import cn.intentforge.session.registry.SessionManager;
import cn.intentforge.space.ResolvedSpaceProfile;
import cn.intentforge.space.SpaceResolver;
import cn.intentforge.tool.core.gateway.ToolGateway;
import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default gateway that resolves runtime context, delegates routing, and executes routed stages.
 */
public final class DefaultAgentGateway implements AgentGateway {
  private final SessionManager sessionManager;
  private final SpaceResolver spaceResolver;
  private final PromptManager promptManager;
  private final ModelManager modelManager;
  private final ModelProviderRegistry modelProviderRegistry;
  private final ToolGateway toolGateway;
  private final AgentRouter agentRouter;
  private final Map<String, AgentExecutor> executorsById;
  private final List<AgentDescriptor> descriptors;

  /**
   * Creates the gateway with required runtime collaborators.
   *
   * @param sessionManager session manager
   * @param spaceResolver space resolver
   * @param promptManager prompt manager
   * @param modelManager model manager
   * @param modelProviderRegistry model provider registry
   * @param toolGateway tool gateway
   * @param agentRouter route selector
   * @param executors available agent executors
   */
  public DefaultAgentGateway(
      SessionManager sessionManager,
      SpaceResolver spaceResolver,
      PromptManager promptManager,
      ModelManager modelManager,
      ModelProviderRegistry modelProviderRegistry,
      ToolGateway toolGateway,
      AgentRouter agentRouter,
      List<AgentExecutor> executors
  ) {
    this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager must not be null");
    this.spaceResolver = Objects.requireNonNull(spaceResolver, "spaceResolver must not be null");
    this.promptManager = Objects.requireNonNull(promptManager, "promptManager must not be null");
    this.modelManager = Objects.requireNonNull(modelManager, "modelManager must not be null");
    this.modelProviderRegistry = Objects.requireNonNull(modelProviderRegistry, "modelProviderRegistry must not be null");
    this.toolGateway = Objects.requireNonNull(toolGateway, "toolGateway must not be null");
    this.agentRouter = Objects.requireNonNull(agentRouter, "agentRouter must not be null");
    this.executorsById = indexExecutors(executors);
    this.descriptors = this.executorsById.values().stream().map(AgentExecutor::descriptor).toList();
  }

  /**
   * Executes one task through routing and stage execution.
   *
   * @param task task request
   * @return routed execution result
   */
  @Override
  public AgentRunResult execute(AgentTask task) {
    AgentTask nonNullTask = Objects.requireNonNull(task, "task must not be null");
    Session session = sessionManager.find(nonNullTask.sessionId())
        .orElseThrow(() -> new AgentExecutionException("session not found: " + nonNullTask.sessionId()));
    String effectiveSpaceId = nonNullTask.spaceId() == null ? session.spaceId() : nonNullTask.spaceId();
    if (effectiveSpaceId == null) {
      throw new AgentExecutionException("spaceId must be available from task or session");
    }

    AgentTask effectiveTask = nonNullTask.spaceId() == null
        ? new AgentTask(
            nonNullTask.id(),
            nonNullTask.sessionId(),
            effectiveSpaceId,
            nonNullTask.workspaceRoot(),
            nonNullTask.mode(),
            nonNullTask.intent(),
            nonNullTask.targetAgentId(),
            nonNullTask.metadata())
        : nonNullTask;

    ResolvedSpaceProfile resolvedSpaceProfile = spaceResolver.resolve(effectiveSpaceId);
    ContextPack contextPack = new ContextPack(
        effectiveTask,
        session,
        resolvedSpaceProfile,
        resolvePrompts(resolvedSpaceProfile),
        resolveModels(resolvedSpaceProfile),
        resolveProviders(resolvedSpaceProfile),
        resolveTools(resolvedSpaceProfile),
        ToolExecutionContext.create(effectiveTask.workspaceRoot()));

    AgentRoute route = agentRouter.route(effectiveTask, contextPack, descriptors);
    AgentExecutionState state = AgentExecutionState.empty();
    for (var step : route.steps()) {
      AgentExecutor executor = executorsById.get(step.agentId());
      if (executor == null) {
        throw new AgentExecutionException("route references unknown agent: " + step.agentId());
      }
      AgentStepResult stepResult = executor.execute(contextPack, state);
      state = state.merge(stepResult);
    }
    String summary = state.decisions().isEmpty() ? "agent execution completed" : state.decisions().getLast().summary();
    return new AgentRunResult(
        contextPack,
        route,
        state.plan(),
        state.decisions(),
        state.artifacts(),
        state.toolCalls(),
        summary);
  }

  /**
   * Lists currently registered agent descriptors.
   *
   * @return immutable descriptor list
   */
  @Override
  public List<AgentDescriptor> listAgents() {
    return descriptors;
  }

  private List<PromptDefinition> resolvePrompts(ResolvedSpaceProfile resolvedSpaceProfile) {
    if (resolvedSpaceProfile.promptIds().isEmpty()) {
      return promptManager.list(null);
    }
    List<PromptDefinition> resolved = new ArrayList<>();
    for (String promptId : resolvedSpaceProfile.promptIds()) {
      promptManager.findLatest(promptId).ifPresent(resolved::add);
    }
    return List.copyOf(resolved);
  }

  private List<ModelProviderDescriptor> resolveProviders(ResolvedSpaceProfile resolvedSpaceProfile) {
    if (resolvedSpaceProfile.modelProviderIds().isEmpty()) {
      return modelProviderRegistry.list();
    }
    List<ModelProviderDescriptor> resolved = new ArrayList<>();
    for (String providerId : resolvedSpaceProfile.modelProviderIds()) {
      ModelProvider provider = modelProviderRegistry.find(providerId).orElse(null);
      if (provider != null) {
        resolved.add(provider.descriptor());
      }
    }
    return List.copyOf(resolved);
  }

  private List<ModelDescriptor> resolveModels(ResolvedSpaceProfile resolvedSpaceProfile) {
    Set<String> allowedProviderIds = new LinkedHashSet<>(resolvedSpaceProfile.modelProviderIds());
    if (resolvedSpaceProfile.modelIds().isEmpty()) {
      return filterModels(modelManager.list(null), allowedProviderIds);
    }
    List<ModelDescriptor> resolved = new ArrayList<>();
    for (String modelId : resolvedSpaceProfile.modelIds()) {
      ModelDescriptor descriptor = modelManager.find(modelId).orElse(null);
      if (descriptor == null) {
        continue;
      }
      if (!allowedProviderIds.isEmpty() && descriptor.providerId() != null && !allowedProviderIds.contains(descriptor.providerId())) {
        continue;
      }
      resolved.add(descriptor);
    }
    return List.copyOf(resolved);
  }

  private List<ModelDescriptor> filterModels(List<ModelDescriptor> candidates, Set<String> allowedProviderIds) {
    if (allowedProviderIds.isEmpty()) {
      return List.copyOf(candidates);
    }
    List<ModelDescriptor> filtered = new ArrayList<>();
    for (ModelDescriptor descriptor : candidates) {
      if (descriptor.providerId() == null || allowedProviderIds.contains(descriptor.providerId())) {
        filtered.add(descriptor);
      }
    }
    return List.copyOf(filtered);
  }

  private List<ToolDefinition> resolveTools(ResolvedSpaceProfile resolvedSpaceProfile) {
    List<ToolDefinition> tools = toolGateway.listTools();
    if (resolvedSpaceProfile.toolIds().isEmpty()) {
      return tools;
    }
    Map<String, ToolDefinition> toolsById = new LinkedHashMap<>();
    for (ToolDefinition tool : tools) {
      toolsById.put(tool.id(), tool);
    }
    List<ToolDefinition> resolved = new ArrayList<>();
    for (String toolId : resolvedSpaceProfile.toolIds()) {
      ToolDefinition toolDefinition = toolsById.get(toolId);
      if (toolDefinition != null) {
        resolved.add(toolDefinition);
      }
    }
    return List.copyOf(resolved);
  }

  private static Map<String, AgentExecutor> indexExecutors(List<AgentExecutor> executors) {
    List<AgentExecutor> nonNullExecutors = List.copyOf(Objects.requireNonNull(executors, "executors must not be null"));
    Map<String, AgentExecutor> indexed = new LinkedHashMap<>();
    for (AgentExecutor executor : nonNullExecutors) {
      AgentExecutor nonNullExecutor = Objects.requireNonNull(executor, "executor must not be null");
      String agentId = nonNullExecutor.descriptor().id();
      AgentExecutor previous = indexed.putIfAbsent(agentId, nonNullExecutor);
      if (previous != null) {
        throw new IllegalArgumentException("duplicate executor id: " + agentId);
      }
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(indexed));
  }
}
