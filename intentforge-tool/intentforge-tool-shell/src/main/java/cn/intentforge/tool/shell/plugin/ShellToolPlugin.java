package cn.intentforge.tool.shell.plugin;

import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.registry.ToolRegistration;
import cn.intentforge.tool.core.spi.ToolPlugin;
import cn.intentforge.tool.shell.ShellExecToolHandler;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides shell execution tool.
 */
public final class ShellToolPlugin implements ToolPlugin {
  /**
   * Tool id for shell execution.
   */
  public static final String TOOL_ID = "intentforge.shell.exec";

  @Override
  public Collection<ToolRegistration> tools() {
    Map<String, Object> schema = Map.of(
        "type", "object",
        "properties", Map.of(
            "command", Map.of("type", "string", "description", "shell command to execute"),
            "workdir", Map.of("type", "string", "description", "working directory inside workspace"),
            "timeoutMs", Map.of("type", "integer", "minimum", 1),
            "loginShell", Map.of("type", "boolean"),
            "tty", Map.of("type", "boolean")
        ),
        "required", List.of("command")
    );

    ToolDefinition definition = new ToolDefinition(
        TOOL_ID,
        "Execute shell command with timeout and safety validation.",
        schema,
        true);
    return List.of(new ToolRegistration(definition, new ShellExecToolHandler()));
  }
}
