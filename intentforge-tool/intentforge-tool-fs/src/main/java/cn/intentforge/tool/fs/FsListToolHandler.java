package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.util.OutputTruncator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.fs.list}.
 */
final class FsListToolHandler implements ToolHandler {
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      Path directory = FsToolSupport.resolvePath(request.context(), request.parameters(), "path", false);
      if (!Files.exists(directory)) {
        return ToolCallResult.error("FS_NOT_FOUND", "Directory does not exist: " + directory);
      }
      if (!Files.isDirectory(directory)) {
        return ToolCallResult.error("FS_NOT_DIRECTORY", "Path is not a directory: " + directory);
      }

      List<Path> entries;
      try (var stream = Files.list(directory)) {
        entries = stream.sorted(Comparator.comparing(path -> path.getFileName().toString())).toList();
      }

      List<String> normalizedEntries = new ArrayList<>();
      StringBuilder outputBuilder = new StringBuilder();
      for (Path entry : entries) {
        String normalized = entry.getFileName().toString() + (Files.isDirectory(entry) ? "/" : "");
        normalizedEntries.add(normalized);
        outputBuilder.append(normalized).append('\n');
      }

      OutputTruncator.TruncateResult truncation =
          OutputTruncator.truncate(outputBuilder.toString(), request.context(), "fs-list");
      Map<String, Object> structured = new LinkedHashMap<>();
      structured.put("path", FsToolSupport.relativePath(request.context(), directory));
      structured.put("entries", List.copyOf(normalizedEntries));
      structured.put("count", normalizedEntries.size());

      Map<String, Object> metadata = new LinkedHashMap<>();
      metadata.put("truncated", truncation.truncated());
      if (truncation.outputPath() != null) {
        metadata.put("outputPath", truncation.outputPath().toString());
      }
      return ToolCallResult.success(truncation.content(), Map.copyOf(structured), Map.copyOf(metadata));
    } catch (IOException ex) {
      return ToolCallResult.error("FS_LIST_ERROR", ex.getMessage());
    }
  }
}
