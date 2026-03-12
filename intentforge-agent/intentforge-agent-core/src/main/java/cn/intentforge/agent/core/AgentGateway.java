package cn.intentforge.agent.core;

import java.util.List;

/**
 * Entry point used to execute routed coding tasks.
 */
public interface AgentGateway {
  /**
   * Executes one task through the configured route and agent executors.
   *
   * @param task task request
   * @return routed execution result
   */
  AgentRunResult execute(AgentTask task);

  /**
   * Lists currently registered agent descriptors.
   *
   * @return immutable descriptor list
   */
  List<AgentDescriptor> listAgents();
}
