package cn.intentforge.agent.nativejava;

import cn.intentforge.agent.core.AgentExecutor;
import cn.intentforge.tool.core.gateway.ToolGateway;
import java.util.List;
import java.util.Objects;

/**
 * Factory for the default native Java coding-agent executor set.
 */
public final class NativeCodingAgentFactory {
  private NativeCodingAgentFactory() {
  }

  /**
   * Creates the default planner, coder, and reviewer executors.
   *
   * @param toolGateway tool gateway used by the coder stage
   * @return immutable executor list
   */
  public static List<AgentExecutor> createDefaultExecutors(ToolGateway toolGateway) {
    ToolGateway nonNullToolGateway = Objects.requireNonNull(toolGateway, "toolGateway must not be null");
    return List.of(
        new NativePlannerAgent(),
        new NativeCoderAgent(nonNullToolGateway),
        new NativeReviewerAgent());
  }
}
