package cn.intentforge.governance.agent;

import cn.intentforge.agent.core.AgentDescriptor;
import cn.intentforge.agent.core.AgentRoute;
import cn.intentforge.agent.core.AgentTask;
import cn.intentforge.agent.core.ContextPack;
import java.util.List;

/**
 * Selects an execution route for one agent task.
 */
public interface AgentRouter {
  /**
   * Selects a route for the provided task and context.
   *
   * @param task task request
   * @param contextPack resolved context
   * @param availableAgents available agent descriptors
   * @return selected route
   */
  AgentRoute route(AgentTask task, ContextPack contextPack, List<AgentDescriptor> availableAgents);
}
