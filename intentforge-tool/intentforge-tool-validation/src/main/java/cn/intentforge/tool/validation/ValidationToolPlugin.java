package cn.intentforge.tool.validation;

import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.registry.ToolRegistration;
import cn.intentforge.tool.core.spi.ToolPlugin;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides build/test/lint validation tools.
 */
public final class ValidationToolPlugin implements ToolPlugin {
  /**
   * Tool id: build validation.
   */
  public static final String TOOL_BUILD = "intentforge.validation.build";
  /**
   * Tool id: test validation.
   */
  public static final String TOOL_TEST = "intentforge.validation.test";
  /**
   * Tool id: lint validation.
   */
  public static final String TOOL_LINT = "intentforge.validation.lint";

  private static final Map<String, Object> COMMON_SCHEMA = Map.of(
      "type", "object",
      "properties", Map.of(
          "command", Map.of("type", "string"),
          "workdir", Map.of("type", "string"),
          "timeoutMs", Map.of("type", "integer", "minimum", 1)));

  @Override
  public Collection<ToolRegistration> tools() {
    return List.of(
        new ToolRegistration(
            new ToolDefinition(
                TOOL_BUILD,
                "Run project build validation.",
                COMMON_SCHEMA,
                true),
            new ValidationCommandToolHandler("build", "./mvnw -q -DskipTests package", 600_000)),
        new ToolRegistration(
            new ToolDefinition(
                TOOL_TEST,
                "Run project test validation.",
                COMMON_SCHEMA,
                true),
            new ValidationCommandToolHandler("test", "./mvnw -q test", 900_000)),
        new ToolRegistration(
            new ToolDefinition(
                TOOL_LINT,
                "Run project lint/static-check validation.",
                COMMON_SCHEMA,
                true),
            new ValidationCommandToolHandler("lint", "./mvnw -q -DskipTests verify", 600_000)));
  }
}
