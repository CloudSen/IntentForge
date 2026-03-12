package cn.intentforge.tool.core.registry;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolDefinition;
import java.util.Objects;

/**
 * One tool definition and its handler.
 *
 * @param definition tool definition
 * @param handler tool handler
 */
public record ToolRegistration(
    ToolDefinition definition,
    ToolHandler handler
) {
  /**
   * Creates a tool registration.
   *
   * @param definition tool definition
   * @param handler tool handler
   */
  public ToolRegistration {
    definition = Objects.requireNonNull(definition, "definition must not be null");
    handler = Objects.requireNonNull(handler, "handler must not be null");
  }
}
