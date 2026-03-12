package cn.intentforge.tool.mcp;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import cn.intentforge.tool.core.registry.ToolRegistration;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class McpToolBridgeTest {
  @Test
  void shouldBridgeSchemaAndExecuteRemoteTool() throws Exception {
    McpClient client = new McpClient() {
      @Override
      public List<McpRemoteTool> listTools() {
        return List.of(new McpRemoteTool(
            "repo/list",
            "list repositories",
            Map.of(
                "type", "object",
                "properties", Map.of("owner", Map.of("type", "string")),
                "required", List.of("owner")),
            false));
      }

      @Override
      public McpRemoteCallResult callTool(String remoteToolId, Map<String, Object> arguments, ToolExecutionContext context) {
        return McpRemoteCallResult.success("ok", Map.of("remoteToolId", remoteToolId, "arguments", arguments), Map.of());
      }
    };
    McpToolBridge bridge = new McpToolBridge(client);
    List<ToolRegistration> registrations = bridge.bridgeTools().stream().toList();
    Assertions.assertEquals(1, registrations.size());
    ToolRegistration registration = registrations.getFirst();
    Assertions.assertEquals("intentforge.mcp.repo-list", registration.definition().id());

    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("mcp-bridge"));
    var result = registration.handler().handle(new ToolCallRequest(
        registration.definition().id(),
        Map.of("owner", "intentforge"),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertEquals("ok", result.output());
    @SuppressWarnings("unchecked")
    Map<String, Object> structured = (Map<String, Object>) result.structured();
    Assertions.assertEquals("repo/list", structured.get("remoteToolId"));
  }

  @Test
  void shouldConvertRemoteError() throws Exception {
    McpClient client = new McpClient() {
      @Override
      public List<McpRemoteTool> listTools() {
        return List.of(new McpRemoteTool("task/run", "run task", Map.of("type", "object"), true));
      }

      @Override
      public McpRemoteCallResult callTool(String remoteToolId, Map<String, Object> arguments, ToolExecutionContext context) {
        return McpRemoteCallResult.error("REMOTE_FAILED", "remote error");
      }
    };
    ToolRegistration registration = new McpToolBridge(client).bridgeTools().stream().findFirst().orElseThrow();
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("mcp-bridge-error"));

    var result = registration.handler().handle(new ToolCallRequest(
        registration.definition().id(),
        Map.of(),
        context));

    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("REMOTE_FAILED", result.errorCode());
  }
}
