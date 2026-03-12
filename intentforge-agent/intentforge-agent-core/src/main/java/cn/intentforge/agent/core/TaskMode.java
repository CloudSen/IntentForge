package cn.intentforge.agent.core;

/**
 * Declares the execution depth requested for one agent task.
 */
public enum TaskMode {
  /**
   * Run the planner stage only.
   */
  PLAN_ONLY,
  /**
   * Run the planner and coder stages.
   */
  IMPLEMENT_ONLY,
  /**
   * Run the reviewer stage only.
   */
  REVIEW_ONLY,
  /**
   * Run the planner, coder, and reviewer stages.
   */
  FULL
}
