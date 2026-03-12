package cn.intentforge.agent.core;

/**
 * Declares the role played by one executor in the routed agent flow.
 */
public enum AgentRole {
  /**
   * Breaks intent into an actionable plan.
   */
  PLANNER,
  /**
   * Turns the plan into concrete implementation steps.
   */
  CODER,
  /**
   * Reviews the implementation outcome.
   */
  REVIEWER,
  /**
   * Reserved for future arbitration or approval stages.
   */
  JUDGE
}
