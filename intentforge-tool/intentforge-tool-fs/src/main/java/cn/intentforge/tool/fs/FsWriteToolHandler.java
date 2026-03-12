package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import java.nio.file.Path;
import java.util.Map;

/**
 * Handler for {@code intentforge.fs.write}.
 */
final class FsWriteToolHandler implements ToolHandler {
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      Path path = FsToolSupport.resolvePath(request.context(), request.parameters(), "path", true);
      String content = FsToolSupport.readString(request.parameters(), "content");
      if (content == null) {
        content = "";
      }
      FsToolSupport.writeText(path, content);
      return ToolCallResult.success(
          "Wrote file: " + FsToolSupport.relativePath(request.context(), path),
          Map.of(
              "path", FsToolSupport.relativePath(request.context(), path),
              "length", content.length()),
          Map.of());
    } catch (Exception ex) {
      return ToolCallResult.error("FS_WRITE_ERROR", ex.getMessage());
    }
  }
}
