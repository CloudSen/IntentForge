package cn.intentforge.agent.core;

import cn.intentforge.tool.core.model.ToolCallResult;
import java.util.List;
import java.util.Objects;

/**
 * Output produced by one routed agent stage.
 *
 * @param plan optional updated plan
 * @param decision required stage decision
 * @param artifacts stage artifacts
 * @param toolCalls tool call results emitted by the stage
 */
public record AgentStepResult(
    Plan plan,
    Decision decision,
    List<Artifact> artifacts,
    List<ToolCallResult> toolCalls
) {
  /**
   * Creates a validated step result.
   */
  public AgentStepResult {
    decision = Objects.requireNonNull(decision, "decision must not be null");
    artifacts = AgentModelSupport.immutableList(artifacts, "artifacts");
    toolCalls = AgentModelSupport.immutableList(toolCalls, "toolCalls");
  }
}
