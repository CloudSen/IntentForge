package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.util.OutputTruncator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.fs.read}.
 */
final class FsReadToolHandler implements ToolHandler {
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      Path path = FsToolSupport.resolvePath(request.context(), request.parameters(), "path", true);
      if (!Files.exists(path)) {
        return ToolCallResult.error("FS_NOT_FOUND", "File does not exist: " + path);
      }
      if (!Files.isRegularFile(path)) {
        return ToolCallResult.error("FS_NOT_FILE", "Path is not a regular file: " + path);
      }
      if (FsToolSupport.isBinaryFile(path)) {
        return ToolCallResult.error("FS_BINARY_FILE", "Binary files are not supported by read tool: " + path);
      }

      List<String> lines = Files.readAllLines(path);
      int totalLines = lines.size();
      int startLine = FsToolSupport.readInt(request.parameters(), "startLine", 1);
      int endLine = FsToolSupport.readInt(request.parameters(), "endLine", totalLines == 0 ? 1 : totalLines);

      if (startLine < 1 || endLine < 1) {
        return ToolCallResult.error("FS_INVALID_RANGE", "startLine/endLine must be >= 1");
      }
      if (endLine < startLine) {
        return ToolCallResult.error("FS_INVALID_RANGE", "endLine must be >= startLine");
      }
      if (startLine > Math.max(1, totalLines)) {
        return ToolCallResult.error("FS_INVALID_RANGE", "startLine exceeds total lines: " + totalLines);
      }
      int normalizedEnd = Math.min(endLine, Math.max(1, totalLines));
      if (totalLines == 0) {
        normalizedEnd = 0;
      }

      StringBuilder outputBuilder = new StringBuilder();
      if (totalLines > 0) {
        for (int lineIndex = startLine; lineIndex <= normalizedEnd; lineIndex++) {
          outputBuilder
              .append(lineIndex)
              .append(": ")
              .append(lines.get(lineIndex - 1))
              .append('\n');
        }
      }
      OutputTruncator.TruncateResult truncation =
          OutputTruncator.truncate(outputBuilder.toString(), request.context(), "fs-read");

      Map<String, Object> structured = new LinkedHashMap<>();
      structured.put("path", FsToolSupport.relativePath(request.context(), path));
      structured.put("startLine", startLine);
      structured.put("endLine", normalizedEnd);
      structured.put("totalLines", totalLines);

      Map<String, Object> metadata = new LinkedHashMap<>();
      metadata.put("truncated", truncation.truncated());
      if (truncation.outputPath() != null) {
        metadata.put("outputPath", truncation.outputPath().toString());
      }
      return ToolCallResult.success(truncation.content(), Map.copyOf(structured), Map.copyOf(metadata));
    } catch (Exception ex) {
      return ToolCallResult.error("FS_READ_ERROR", ex.getMessage());
    }
  }
}
