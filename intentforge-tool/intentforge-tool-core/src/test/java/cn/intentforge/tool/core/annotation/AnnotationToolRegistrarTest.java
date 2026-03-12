package cn.intentforge.tool.core.annotation;

import cn.intentforge.tool.core.gateway.DefaultToolGateway;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import cn.intentforge.tool.core.permission.DefaultToolPermissionPolicy;
import cn.intentforge.tool.core.registry.InMemoryToolRegistry;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AnnotationToolRegistrarTest {
  static class DemoTools {
    @IntentTool(id = "tool.echo", description = "echo")
    String echo(@IntentToolParam(name = "message") String message) {
      return "ECHO:" + message;
    }
  }

  @Test
  void shouldRegisterAnnotatedToolAndExecute() throws Exception {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    AnnotationToolRegistrar registrar = new AnnotationToolRegistrar(registry);
    registrar.register(new DemoTools());

    DefaultToolGateway gateway = new DefaultToolGateway(registry, new DefaultToolPermissionPolicy());
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("annotation-tool"));

    var result = gateway.execute(new ToolCallRequest("tool.echo", Map.of("message", "hello"), context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertEquals("ECHO:hello", result.output());
  }
}
