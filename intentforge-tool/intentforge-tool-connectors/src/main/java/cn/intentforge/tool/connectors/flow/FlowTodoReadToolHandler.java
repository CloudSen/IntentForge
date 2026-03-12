package cn.intentforge.tool.connectors.flow;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.flow.todo.read}.
 */
public final class FlowTodoReadToolHandler implements ToolHandler {
  /**
   * Reads todo items from execution context.
   *
   * @param request tool request
   * @return execution result
   */
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    List<String> todos = FlowToolSupport.readTodos(request.context());
    StringBuilder output = new StringBuilder();
    if (todos.isEmpty()) {
      output.append("Todo list is empty");
    } else {
      int index = 1;
      for (String todo : todos) {
        output.append(index++).append(". ").append(todo).append('\n');
      }
    }
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("count", todos.size());
    structured.put("items", List.copyOf(todos));
    return ToolCallResult.success(output.toString().trim(), Map.copyOf(structured), Map.of());
  }
}
