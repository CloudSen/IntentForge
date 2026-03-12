package cn.intentforge.tool.core;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;

/**
 * Handles one tool invocation.
 */
@FunctionalInterface
public interface ToolHandler {
  /**
   * Executes the tool call.
   *
   * @param request tool call request
   * @return tool call result
   */
  ToolCallResult handle(ToolCallRequest request);
}
