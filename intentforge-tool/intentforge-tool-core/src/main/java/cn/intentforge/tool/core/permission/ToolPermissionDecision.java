package cn.intentforge.tool.core.permission;

/**
 * Permission decision for one tool call.
 */
public enum ToolPermissionDecision {
  /**
   * Tool call is allowed.
   */
  ALLOW,
  /**
   * Tool call is denied.
   */
  DENY,
  /**
   * Tool call should be suspended and wait for external confirmation.
   */
  ASK
}
