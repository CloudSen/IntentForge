package cn.intentforge.tool.connectors.flow;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for {@code intentforge.flow.task.dispatch}.
 */
public final class FlowTaskDispatchToolHandler implements ToolHandler {
  /**
   * Stores one dispatch request and returns suspended status for runtime orchestration.
   *
   * @param request tool request
   * @return suspended result
   */
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    String target = FlowToolSupport.readString(request.parameters(), "target");
    String task = FlowToolSupport.readString(request.parameters(), "task");
    if (target == null || task == null) {
      return ToolCallResult.error("FLOW_INVALID_ARGUMENT", "target and task are required");
    }
    String dispatchId = FlowToolSupport.readString(request.parameters(), "dispatchId");
    if (dispatchId == null) {
      dispatchId = "dispatch-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID();
    }

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("dispatchId", dispatchId);
    payload.put("target", target);
    payload.put("task", task);
    Object rawPayload = request.parameters().get("payload");
    if (rawPayload != null) {
      payload.put("payload", rawPayload);
    }
    payload.put("createdAt", Instant.now().toString());

    List<Map<String, Object>> dispatches = new ArrayList<>(FlowToolSupport.readDispatches(request.context()));
    dispatches.add(Map.copyOf(payload));
    FlowToolSupport.writeDispatches(request.context(), dispatches);

    return ToolCallResult.suspended("Task dispatched: " + dispatchId, Map.copyOf(payload));
  }
}
