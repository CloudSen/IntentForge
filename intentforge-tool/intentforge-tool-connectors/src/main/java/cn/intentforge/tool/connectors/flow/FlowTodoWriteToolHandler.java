package cn.intentforge.tool.connectors.flow;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.flow.todo.write}.
 */
public final class FlowTodoWriteToolHandler implements ToolHandler {
  /**
   * Writes todo items into execution context.
   *
   * @param request tool request
   * @return execution result
   */
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    List<String> incoming = FlowToolSupport.readStringList(request.parameters().get("items"));
    if (incoming.isEmpty()) {
      return ToolCallResult.error("FLOW_INVALID_ARGUMENT", "items is required");
    }
    boolean append = FlowToolSupport.readBoolean(request.parameters(), "append", true);

    List<String> todos = append ? new ArrayList<>(FlowToolSupport.readTodos(request.context())) : new ArrayList<>();
    todos.addAll(incoming);
    FlowToolSupport.writeTodos(request.context(), List.copyOf(todos));

    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("count", todos.size());
    structured.put("items", List.copyOf(todos));
    structured.put("append", append);
    return ToolCallResult.success("Todo list updated: " + todos.size() + " items", Map.copyOf(structured), Map.of());
  }
}
