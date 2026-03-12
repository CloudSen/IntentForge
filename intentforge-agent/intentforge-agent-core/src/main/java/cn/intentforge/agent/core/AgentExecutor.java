package cn.intentforge.agent.core;

/**
 * Executes one routed agent stage.
 */
public interface AgentExecutor {
  /**
   * Returns public metadata for this executor.
   *
   * @return executor descriptor
   */
  AgentDescriptor descriptor();

  /**
   * Executes one stage using the shared context and accumulated state.
   *
   * @param contextPack resolved execution context
   * @param state accumulated execution state before this stage
   * @return stage result
   */
  AgentStepResult execute(ContextPack contextPack, AgentExecutionState state);
}
