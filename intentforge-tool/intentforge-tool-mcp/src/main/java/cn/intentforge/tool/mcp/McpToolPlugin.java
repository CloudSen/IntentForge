package cn.intentforge.tool.mcp;

import cn.intentforge.tool.core.model.ToolExecutionContext;
import cn.intentforge.tool.core.registry.ToolRegistration;
import cn.intentforge.tool.core.spi.ToolPlugin;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * MCP tool plugin that bridges remote MCP tools into local registrations.
 */
public final class McpToolPlugin implements ToolPlugin {
  private final McpToolBridge bridge;

  /**
   * Creates plugin with no-op MCP client.
   */
  public McpToolPlugin() {
    this(new NoopMcpClient());
  }

  /**
   * Creates plugin.
   *
   * @param client MCP client
   */
  public McpToolPlugin(McpClient client) {
    this.bridge = new McpToolBridge(client);
  }

  @Override
  public Collection<ToolRegistration> tools() {
    return bridge.bridgeTools();
  }

  private static final class NoopMcpClient implements McpClient {
    @Override
    public List<McpRemoteTool> listTools() {
      return List.of();
    }

    @Override
    public McpRemoteCallResult callTool(String remoteToolId, Map<String, Object> arguments, ToolExecutionContext context) {
      return McpRemoteCallResult.error("MCP_NOT_CONFIGURED", "MCP client is not configured");
    }
  }
}
