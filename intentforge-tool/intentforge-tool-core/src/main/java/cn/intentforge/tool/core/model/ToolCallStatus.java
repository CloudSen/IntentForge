package cn.intentforge.tool.core.model;

/**
 * Status of one tool call.
 */
public enum ToolCallStatus {
  /**
   * The tool call finished successfully.
   */
  SUCCESS,
  /**
   * The tool call failed.
   */
  ERROR,
  /**
   * The tool call is suspended and waits for external action.
   */
  SUSPENDED
}
