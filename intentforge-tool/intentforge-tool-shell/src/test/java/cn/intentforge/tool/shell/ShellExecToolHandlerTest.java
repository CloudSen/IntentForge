package cn.intentforge.tool.shell;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import cn.intentforge.tool.core.registry.InMemoryToolRegistry;
import cn.intentforge.tool.shell.plugin.ShellToolPlugin;
import cn.intentforge.tool.shell.security.DefaultShellCommandValidator;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShellExecToolHandlerTest {
  @Test
  void shouldExecuteCommand() throws Exception {
    ShellExecToolHandler handler = new ShellExecToolHandler(Set.of(), new DefaultShellCommandValidator(), 5_000, 4_000);
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("shell-tool"));

    var result = handler.handle(new ToolCallRequest("intentforge.shell.exec", Map.of("command", "echo hello"), context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertTrue(result.output().contains("hello"));
  }

  @Test
  void shouldRejectMultipleCommands() throws Exception {
    ShellExecToolHandler handler = new ShellExecToolHandler(Set.of(), new DefaultShellCommandValidator(), 5_000, 4_000);
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("shell-tool"));

    var result = handler.handle(new ToolCallRequest(
        "intentforge.shell.exec",
        Map.of("command", "echo hello; echo world"),
        context));
    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("SHELL_COMMAND_REJECTED", result.errorCode());
  }

  @Test
  void shouldRejectNonWhitelistedExecutable() throws Exception {
    ShellExecToolHandler handler = new ShellExecToolHandler(Set.of("ls"), new DefaultShellCommandValidator(), 5_000, 4_000);
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("shell-tool"));

    var result = handler.handle(new ToolCallRequest("intentforge.shell.exec", Map.of("command", "echo hello"), context));
    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("SHELL_COMMAND_REJECTED", result.errorCode());
  }

  @Test
  void shouldLoadPluginThroughServiceLoader() {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    registry.loadPlugins();
    Assertions.assertTrue(registry.find(ShellToolPlugin.TOOL_ID).isPresent());
  }
}
