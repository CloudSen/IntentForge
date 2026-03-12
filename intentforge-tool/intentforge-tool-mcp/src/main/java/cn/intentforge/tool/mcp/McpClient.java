package cn.intentforge.tool.mcp;

import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.util.List;
import java.util.Map;

/**
 * MCP client abstraction used by tool bridge.
 */
public interface McpClient {
  /**
   * Lists remote MCP tools.
   *
   * @return remote tool descriptors
   */
  List<McpRemoteTool> listTools();

  /**
   * Calls one remote MCP tool.
   *
   * @param remoteToolId remote tool identifier
   * @param arguments tool arguments
   * @param context execution context
   * @return remote call result
   */
  McpRemoteCallResult callTool(String remoteToolId, Map<String, Object> arguments, ToolExecutionContext context);
}
