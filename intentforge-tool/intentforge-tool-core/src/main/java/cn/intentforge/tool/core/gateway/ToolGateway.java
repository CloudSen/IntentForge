package cn.intentforge.tool.core.gateway;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.model.ToolDefinition;
import java.util.List;

/**
 * Entry point for tool execution.
 */
public interface ToolGateway {
  /**
   * Executes one tool call request.
   *
   * @param request tool request
   * @return tool execution result
   */
  ToolCallResult execute(ToolCallRequest request);

  /**
   * Lists currently registered tool definitions.
   *
   * @return immutable tool definition list
   */
  List<ToolDefinition> listTools();
}
