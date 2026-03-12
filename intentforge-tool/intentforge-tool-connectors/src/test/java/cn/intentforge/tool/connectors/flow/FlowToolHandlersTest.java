package cn.intentforge.tool.connectors.flow;

import cn.intentforge.tool.connectors.ConnectorToolPlugin;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FlowToolHandlersTest {
  @Test
  void shouldWriteAndReadTodo() throws Exception {
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("flow-todo"));
    FlowTodoWriteToolHandler writeHandler = new FlowTodoWriteToolHandler();
    FlowTodoReadToolHandler readHandler = new FlowTodoReadToolHandler();

    var writeResult = writeHandler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_FLOW_TODO_WRITE,
        Map.of("items", List.of("a", "b"), "append", false),
        context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, writeResult.status());

    var readResult = readHandler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_FLOW_TODO_READ,
        Map.of(),
        context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, readResult.status());
    Assertions.assertTrue(readResult.output().contains("1. a"));
    Assertions.assertTrue(readResult.output().contains("2. b"));
  }

  @Test
  void shouldCreateSuspendedAskAndDispatch() throws Exception {
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("flow-dispatch"));
    FlowAskToolHandler askHandler = new FlowAskToolHandler();
    FlowTaskDispatchToolHandler dispatchHandler = new FlowTaskDispatchToolHandler();

    var askResult = askHandler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_FLOW_ASK,
        Map.of("question", "continue?"),
        context));
    Assertions.assertEquals(ToolCallStatus.SUSPENDED, askResult.status());

    var dispatchResult = dispatchHandler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_FLOW_TASK_DISPATCH,
        Map.of("target", "worker-a", "task", "build"),
        context));
    Assertions.assertEquals(ToolCallStatus.SUSPENDED, dispatchResult.status());
    Assertions.assertTrue(context.attribute(FlowToolSupport.DISPATCH_KEY).isPresent());
  }
}
