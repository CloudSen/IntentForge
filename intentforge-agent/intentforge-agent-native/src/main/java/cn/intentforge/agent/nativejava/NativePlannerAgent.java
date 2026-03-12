package cn.intentforge.agent.nativejava;

import cn.intentforge.agent.core.AgentDescriptor;
import cn.intentforge.agent.core.AgentExecutionState;
import cn.intentforge.agent.core.AgentExecutor;
import cn.intentforge.agent.core.AgentRole;
import cn.intentforge.agent.core.AgentStepResult;
import cn.intentforge.agent.core.ContextPack;
import cn.intentforge.agent.core.Decision;
import cn.intentforge.agent.core.Plan;
import cn.intentforge.agent.core.PlanStep;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Deterministic planner for the native Java coding-agent MVP.
 */
public final class NativePlannerAgent implements AgentExecutor {
  private static final AgentDescriptor DESCRIPTOR = new AgentDescriptor(
      "intentforge.native.planner",
      AgentRole.PLANNER,
      "Native Planner",
      "Builds a minimal coding plan from the resolved runtime context.");

  /**
   * Returns public metadata for this executor.
   *
   * @return executor descriptor
   */
  @Override
  public AgentDescriptor descriptor() {
    return DESCRIPTOR;
  }

  /**
   * Executes the planning stage.
   *
   * @param contextPack resolved execution context
   * @param state accumulated execution state before this stage
   * @return stage result
   */
  @Override
  public AgentStepResult execute(ContextPack contextPack, AgentExecutionState state) {
    ContextPack nonNullContextPack = Objects.requireNonNull(contextPack, "contextPack must not be null");
    Objects.requireNonNull(state, "state must not be null");
    Plan plan = new Plan(
        "MVP native coding plan for: " + nonNullContextPack.task().intent(),
        List.of(
            new PlanStep(
                "context-align",
                "Resolve context",
                "Resolve session, space, prompt, model, provider, and tool bindings before execution.",
                null,
                true),
            new PlanStep(
                "workspace-inspect",
                "Inspect workspace",
                "Inspect the workspace with the preferred non-sensitive tool before implementation.",
                NativeAgentSupport.preferredWorkspaceTool(nonNullContextPack),
                true),
            new PlanStep(
                "review-output",
                "Review outcome",
                "Summarize implementation notes and produce a review artifact for the MVP flow.",
                null,
                true)),
        Map.of(
            "promptId", NativeAgentSupport.firstPromptId(nonNullContextPack),
            "modelId", NativeAgentSupport.firstModelId(nonNullContextPack),
            "providerId", NativeAgentSupport.firstProviderId(nonNullContextPack)));
    return new AgentStepResult(
        plan,
        new Decision(descriptor().id(), descriptor().role(), descriptor().id() + " completed", Map.of("stage", "plan")),
        List.of(),
        List.of());
  }
}
