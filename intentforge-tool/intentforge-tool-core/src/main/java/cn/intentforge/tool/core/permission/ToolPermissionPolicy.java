package cn.intentforge.tool.core.permission;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolExecutionContext;

/**
 * Permission policy used by tool gateway before execution.
 */
public interface ToolPermissionPolicy {
  /**
   * Decides permission for one tool invocation.
   *
   * @param toolId tool identifier
   * @param request tool request
   * @param context execution context
   * @return permission decision
   */
  ToolPermissionDecision decide(String toolId, ToolCallRequest request, ToolExecutionContext context);
}
