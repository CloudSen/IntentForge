package cn.intentforge.tool.validation;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ValidationCommandToolHandlerTest {
  @Test
  void shouldReportSuccess() throws Exception {
    ValidationCommandToolHandler handler = new ValidationCommandToolHandler("build", "echo ok", 5_000);
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("validation-success"));

    var result = handler.handle(new ToolCallRequest(
        ValidationToolPlugin.TOOL_BUILD,
        Map.of(),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    @SuppressWarnings("unchecked")
    Map<String, Object> report = (Map<String, Object>) result.structured();
    Assertions.assertEquals(Boolean.TRUE, report.get("success"));
  }

  @Test
  void shouldReportFailure() throws Exception {
    ValidationCommandToolHandler handler = new ValidationCommandToolHandler("test", "exit 2", 5_000);
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("validation-failure"));

    var result = handler.handle(new ToolCallRequest(
        ValidationToolPlugin.TOOL_TEST,
        Map.of(),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    @SuppressWarnings("unchecked")
    Map<String, Object> report = (Map<String, Object>) result.structured();
    Assertions.assertEquals(Boolean.FALSE, report.get("success"));
  }

  @Test
  void shouldReportTimeout() throws Exception {
    ValidationCommandToolHandler handler = new ValidationCommandToolHandler("lint", "sleep 1", 50);
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("validation-timeout"));

    var result = handler.handle(new ToolCallRequest(
        ValidationToolPlugin.TOOL_LINT,
        Map.of(),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    @SuppressWarnings("unchecked")
    Map<String, Object> report = (Map<String, Object>) result.structured();
    Assertions.assertEquals(Boolean.FALSE, report.get("success"));
    Assertions.assertEquals(Boolean.TRUE, result.metadata().get("timedOut"));
  }
}
