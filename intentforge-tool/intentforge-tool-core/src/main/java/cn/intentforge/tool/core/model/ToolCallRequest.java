package cn.intentforge.tool.core.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Request payload for one tool execution.
 *
 * @param toolId tool identifier
 * @param parameters input parameters
 * @param context execution context
 */
public record ToolCallRequest(
    String toolId,
    Map<String, Object> parameters,
    ToolExecutionContext context
) {
  /**
   * Creates a tool call request.
   *
   * @param toolId tool identifier
   * @param parameters input parameters
   * @param context execution context
   */
  public ToolCallRequest {
    toolId = requireText(toolId, "toolId");
    parameters = immutableParameters(parameters);
    context = Objects.requireNonNull(context, "context must not be null");
  }

  private static Map<String, Object> immutableParameters(Map<String, Object> value) {
    if (value == null || value.isEmpty()) {
      return Map.of();
    }
    return Map.copyOf(new LinkedHashMap<>(value));
  }

  private static String requireText(String value, String fieldName) {
    if (value == null) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    String normalized = value.trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return normalized;
  }
}
