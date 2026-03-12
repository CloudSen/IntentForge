package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import cn.intentforge.tool.core.registry.InMemoryToolRegistry;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FsToolHandlersTest {
  @Test
  void shouldRejectPathEscape() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-escape");
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsWriteToolHandler handler = new FsWriteToolHandler();

    var result = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_WRITE,
        Map.of("path", "../escape.txt", "content", "x"),
        context));

    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("FS_WRITE_ERROR", result.errorCode());
    Assertions.assertTrue(result.errorMessage().contains("escapes workspace"));
  }

  @Test
  void shouldRejectBinaryFileRead() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-binary");
    Path binary = workspace.resolve("data.bin");
    Files.write(binary, new byte[] {0, 1, 2, 3});
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsReadToolHandler handler = new FsReadToolHandler();

    var result = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_READ,
        Map.of("path", "data.bin"),
        context));

    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("FS_BINARY_FILE", result.errorCode());
  }

  @Test
  void shouldReadWithBoundaryRange() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-read");
    Path file = workspace.resolve("a.txt");
    Files.writeString(file, "line1\nline2\nline3\n", StandardCharsets.UTF_8);
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsReadToolHandler handler = new FsReadToolHandler();

    var result = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_READ,
        Map.of("path", "a.txt", "startLine", 2, "endLine", 99),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertTrue(result.output().contains("2: line2"));
    Assertions.assertTrue(result.output().contains("3: line3"));
    @SuppressWarnings("unchecked")
    Map<String, Object> structured = (Map<String, Object>) result.structured();
    Assertions.assertEquals(3, structured.get("endLine"));
  }

  @Test
  void shouldRejectInvalidReadRange() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-range");
    Path file = workspace.resolve("a.txt");
    Files.writeString(file, "line1\n", StandardCharsets.UTF_8);
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsReadToolHandler handler = new FsReadToolHandler();

    var result = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_READ,
        Map.of("path", "a.txt", "startLine", 2, "endLine", 2),
        context));

    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("FS_INVALID_RANGE", result.errorCode());
  }

  @Test
  void shouldEditSingleAndMultipleMatches() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-edit");
    Path file = workspace.resolve("b.txt");
    Files.writeString(file, "foo\nfoo\n", StandardCharsets.UTF_8);
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsEditToolHandler handler = new FsEditToolHandler();

    var ambiguousResult = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_EDIT,
        Map.of("path", "b.txt", "oldString", "foo", "newString", "bar"),
        context));
    Assertions.assertEquals(ToolCallStatus.ERROR, ambiguousResult.status());
    Assertions.assertEquals("FS_EDIT_AMBIGUOUS", ambiguousResult.errorCode());

    var replaceAllResult = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_EDIT,
        Map.of("path", "b.txt", "oldString", "foo", "newString", "bar", "replaceAll", true),
        context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, replaceAllResult.status());
    Assertions.assertEquals("bar\nbar\n", Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void shouldApplyPatchSuccessfully() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-patch");
    Path file = workspace.resolve("demo.txt");
    Files.writeString(file, "hello\nworld\n", StandardCharsets.UTF_8);
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsApplyPatchToolHandler handler = new FsApplyPatchToolHandler();

    String patch = """
        *** Begin Patch
        *** Update File: demo.txt
        @@
        -hello
        +hi
         world
        *** End Patch
        """;
    var result = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_APPLY_PATCH,
        Map.of("patch", patch),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertEquals("hi\nworld\n", Files.readString(file, StandardCharsets.UTF_8));
  }

  @Test
  void shouldRejectInvalidPatch() throws Exception {
    Path workspace = Files.createTempDirectory("fs-tool-badpatch");
    ToolExecutionContext context = ToolExecutionContext.create(workspace);
    FsApplyPatchToolHandler handler = new FsApplyPatchToolHandler();

    var result = handler.handle(new ToolCallRequest(
        FsToolPlugin.TOOL_APPLY_PATCH,
        Map.of("patch", "*** Begin Patch\n*** Unknown: x\n*** End Patch\n"),
        context));

    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("FS_APPLY_PATCH_ERROR", result.errorCode());
  }

  @Test
  void shouldLoadFsPluginViaServiceLoader() {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    registry.loadPlugins();
    Assertions.assertTrue(registry.find(FsToolPlugin.TOOL_READ).isPresent());
    Assertions.assertTrue(registry.find(FsToolPlugin.TOOL_APPLY_PATCH).isPresent());
  }
}
