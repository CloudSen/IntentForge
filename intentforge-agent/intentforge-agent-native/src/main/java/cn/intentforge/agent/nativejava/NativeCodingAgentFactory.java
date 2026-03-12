package cn.intentforge.agent.nativejava;

import cn.intentforge.agent.core.AgentExecutor;
import java.util.List;

/**
 * Factory for the default native Java coding-agent executor set.
 */
public final class NativeCodingAgentFactory {
  private NativeCodingAgentFactory() {
  }

  /**
   * Creates the default planner, coder, and reviewer executors.
   *
   * @return immutable executor list
   */
  public static List<AgentExecutor> createDefaultExecutors() {
    return List.of(
        new NativePlannerAgent(),
        new NativeCoderAgent(),
        new NativeReviewerAgent());
  }
}
