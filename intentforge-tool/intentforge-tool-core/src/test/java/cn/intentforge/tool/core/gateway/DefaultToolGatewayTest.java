package cn.intentforge.tool.core.gateway;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import cn.intentforge.tool.core.permission.DefaultToolPermissionPolicy;
import cn.intentforge.tool.core.registry.InMemoryToolRegistry;
import cn.intentforge.tool.core.registry.ToolRegistration;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DefaultToolGatewayTest {
  @Test
  void shouldHandleNotFoundPermissionAndValidation() throws Exception {
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("tool-gateway"));
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    DefaultToolPermissionPolicy policy = new DefaultToolPermissionPolicy(List.of("tool.sensitive"));
    DefaultToolGateway gateway = new DefaultToolGateway(registry, policy);

    var notFoundResult = gateway.execute(new ToolCallRequest("none", Map.of(), context));
    Assertions.assertEquals(ToolCallStatus.ERROR, notFoundResult.status());
    Assertions.assertEquals(DefaultToolGateway.ERROR_TOOL_NOT_FOUND, notFoundResult.errorCode());

    ToolDefinition sensitiveDefinition = new ToolDefinition(
        "tool.sensitive",
        "d",
        Map.of(
            "type", "object",
            "properties", Map.of("name", Map.of("type", "string")),
            "required", List.of("name")
        ),
        true);
    registry.register(new ToolRegistration(sensitiveDefinition, request -> cn.intentforge.tool.core.model.ToolCallResult.success("ok")));

    var askResult = gateway.execute(new ToolCallRequest("tool.sensitive", Map.of("name", "ok"), context));
    Assertions.assertEquals(ToolCallStatus.SUSPENDED, askResult.status());

    policy.allow("tool.sensitive");
    var invalidResult = gateway.execute(new ToolCallRequest("tool.sensitive", Map.of("name", 1), context));
    Assertions.assertEquals(ToolCallStatus.ERROR, invalidResult.status());
    Assertions.assertEquals(DefaultToolGateway.ERROR_INVALID_PARAMETERS, invalidResult.errorCode());
  }

  @Test
  void shouldExecuteAndConvertToolError() throws Exception {
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("tool-gateway"));
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    DefaultToolPermissionPolicy policy = new DefaultToolPermissionPolicy();
    DefaultToolGateway gateway = new DefaultToolGateway(registry, policy);

    ToolDefinition definition = new ToolDefinition("tool.exec", "d", Map.of(), false);
    registry.register(new ToolRegistration(definition, request -> cn.intentforge.tool.core.model.ToolCallResult.success("OK")));

    var success = gateway.execute(new ToolCallRequest("tool.exec", Map.of(), context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, success.status());
    Assertions.assertEquals("OK", success.output());

    registry.register(new ToolRegistration(definition, request -> {
      throw new IllegalStateException("boom");
    }));
    var failure = gateway.execute(new ToolCallRequest("tool.exec", Map.of(), context));
    Assertions.assertEquals(ToolCallStatus.ERROR, failure.status());
    Assertions.assertEquals(DefaultToolGateway.ERROR_TOOL_EXECUTION_FAILED, failure.errorCode());
  }
}
