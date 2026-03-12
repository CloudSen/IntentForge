package cn.intentforge.tool.connectors.flow;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.flow.ask}.
 */
public final class FlowAskToolHandler implements ToolHandler {
  /**
   * Builds one suspended interaction request.
   *
   * @param request tool request
   * @return suspended result
   */
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    String question = FlowToolSupport.readString(request.parameters(), "question");
    if (question == null) {
      return ToolCallResult.error("FLOW_INVALID_ARGUMENT", "question is required");
    }
    List<String> options = FlowToolSupport.readStringList(request.parameters().get("options"));
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("question", question);
    metadata.put("options", List.copyOf(options));
    return ToolCallResult.suspended(question, Map.copyOf(metadata));
  }
}
