package cn.intentforge.tool.connectors.flow;

import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared helpers for flow tools.
 */
final class FlowToolSupport {
  static final String TODO_KEY = "intentforge.flow.todo.items";
  static final String DISPATCH_KEY = "intentforge.flow.dispatch.items";

  private FlowToolSupport() {
  }

  static String readString(Map<String, Object> parameters, String key) {
    Object value = parameters.get(key);
    if (value == null) {
      return null;
    }
    String normalized = String.valueOf(value).trim();
    return normalized.isEmpty() ? null : normalized;
  }

  static boolean readBoolean(Map<String, Object> parameters, String key, boolean defaultValue) {
    Object value = parameters.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return Boolean.parseBoolean(String.valueOf(value));
  }

  static List<String> readStringList(Object value) {
    if (value == null) {
      return List.of();
    }
    if (value instanceof List<?> list) {
      List<String> normalized = new ArrayList<>();
      for (Object item : list) {
        String text = item == null ? null : String.valueOf(item).trim();
        if (text != null && !text.isEmpty()) {
          normalized.add(text);
        }
      }
      return List.copyOf(normalized);
    }
    String text = String.valueOf(value).trim();
    if (text.isEmpty()) {
      return List.of();
    }
    return List.of(text);
  }

  static List<String> readTodos(ToolExecutionContext context) {
    Object value = context.attribute(TODO_KEY).orElse(List.of());
    return readStringList(value);
  }

  static void writeTodos(ToolExecutionContext context, List<String> items) {
    context.putAttribute(TODO_KEY, List.copyOf(items));
  }

  static List<Map<String, Object>> readDispatches(ToolExecutionContext context) {
    Object value = context.attribute(DISPATCH_KEY).orElse(List.of());
    if (!(value instanceof List<?> list)) {
      return List.of();
    }
    List<Map<String, Object>> dispatches = new ArrayList<>();
    for (Object item : list) {
      if (item instanceof Map<?, ?> map) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          if (entry.getKey() == null) {
            continue;
          }
          normalized.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        dispatches.add(Map.copyOf(normalized));
      }
    }
    return List.copyOf(dispatches);
  }

  static void writeDispatches(ToolExecutionContext context, List<Map<String, Object>> dispatches) {
    context.putAttribute(DISPATCH_KEY, List.copyOf(dispatches));
  }
}
