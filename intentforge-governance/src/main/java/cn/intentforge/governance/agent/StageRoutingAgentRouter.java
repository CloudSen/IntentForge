package cn.intentforge.governance.agent;

import cn.intentforge.agent.core.AgentDescriptor;
import cn.intentforge.agent.core.AgentExecutionException;
import cn.intentforge.agent.core.AgentRole;
import cn.intentforge.agent.core.AgentRoute;
import cn.intentforge.agent.core.AgentRouteStep;
import cn.intentforge.agent.core.AgentTask;
import cn.intentforge.agent.core.ContextPack;
import cn.intentforge.agent.core.TaskMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Default MVP router that selects planner/coder/reviewer stages by task mode.
 */
public final class StageRoutingAgentRouter implements AgentRouter {
  private static final String STRATEGY = "stage-based";

  /**
   * Selects a route for the provided task and context.
   *
   * @param task task request
   * @param contextPack resolved context
   * @param availableAgents available agent descriptors
   * @return selected route
   */
  @Override
  public AgentRoute route(AgentTask task, ContextPack contextPack, List<AgentDescriptor> availableAgents) {
    AgentTask nonNullTask = Objects.requireNonNull(task, "task must not be null");
    ContextPack nonNullContextPack = Objects.requireNonNull(contextPack, "contextPack must not be null");
    List<AgentDescriptor> agents = List.copyOf(Objects.requireNonNull(availableAgents, "availableAgents must not be null"));
    Set<String> allowedAgentIds = new LinkedHashSet<>(nonNullContextPack.resolvedSpaceProfile().agentIds());

    if (nonNullTask.targetAgentId() != null) {
      AgentDescriptor descriptor = selectById(nonNullTask.targetAgentId(), allowedAgentIds, agents);
      return new AgentRoute(
          STRATEGY,
          List.of(new AgentRouteStep(1, descriptor.id(), descriptor.role(), "task requested explicit target agent")));
    }

    List<AgentRole> roles = rolesFor(nonNullTask.mode());
    List<AgentRouteStep> steps = new ArrayList<>(roles.size());
    for (int index = 0; index < roles.size(); index++) {
      AgentRole role = roles.get(index);
      AgentDescriptor descriptor = selectByRole(role, allowedAgentIds, agents);
      steps.add(new AgentRouteStep(index + 1, descriptor.id(), descriptor.role(), "selected by mode " + nonNullTask.mode()));
    }
    return new AgentRoute(STRATEGY, steps);
  }

  private static List<AgentRole> rolesFor(TaskMode mode) {
    return switch (mode) {
      case PLAN_ONLY -> List.of(AgentRole.PLANNER);
      case IMPLEMENT_ONLY -> List.of(AgentRole.PLANNER, AgentRole.CODER);
      case REVIEW_ONLY -> List.of(AgentRole.REVIEWER);
      case FULL -> List.of(AgentRole.PLANNER, AgentRole.CODER, AgentRole.REVIEWER);
    };
  }

  private static AgentDescriptor selectById(
      String agentId,
      Set<String> allowedAgentIds,
      List<AgentDescriptor> availableAgents
  ) {
    for (AgentDescriptor descriptor : availableAgents) {
      if (!descriptor.id().equals(agentId)) {
        continue;
      }
      if (!allowedAgentIds.isEmpty() && !allowedAgentIds.contains(descriptor.id())) {
        throw new AgentExecutionException("target agent is not allowed by the resolved space: " + agentId);
      }
      return descriptor;
    }
    throw new AgentExecutionException("target agent not found: " + agentId);
  }

  private static AgentDescriptor selectByRole(
      AgentRole role,
      Set<String> allowedAgentIds,
      List<AgentDescriptor> availableAgents
  ) {
    for (AgentDescriptor descriptor : availableAgents) {
      if (descriptor.role() != role) {
        continue;
      }
      if (!allowedAgentIds.isEmpty() && !allowedAgentIds.contains(descriptor.id())) {
        continue;
      }
      return descriptor;
    }
    throw new AgentExecutionException("no agent available for role " + role);
  }
}
