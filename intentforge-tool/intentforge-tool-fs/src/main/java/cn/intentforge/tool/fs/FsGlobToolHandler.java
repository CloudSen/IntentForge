package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.util.OutputTruncator;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.fs.glob}.
 */
final class FsGlobToolHandler implements ToolHandler {
  private static final int MAX_RESULTS = 500;

  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      String pattern = FsToolSupport.readString(request.parameters(), "pattern");
      if (pattern == null) {
        return ToolCallResult.error("FS_INVALID_ARGUMENT", "pattern is required");
      }
      Path root = FsToolSupport.resolvePath(request.context(), request.parameters(), "path", false);
      if (!Files.exists(root)) {
        return ToolCallResult.error("FS_NOT_FOUND", "Search path does not exist: " + root);
      }
      if (!Files.isDirectory(root)) {
        return ToolCallResult.error("FS_NOT_DIRECTORY", "Search path is not directory: " + root);
      }

      PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
      List<String> matches = new ArrayList<>();
      try (var stream = Files.walk(root)) {
        for (Path current : stream.toList()) {
          Path relative = root.relativize(current);
          if (relative.toString().isEmpty()) {
            continue;
          }
          if (matcher.matches(relative)) {
            matches.add(FsToolSupport.relativePath(request.context(), current));
            if (matches.size() >= MAX_RESULTS) {
              break;
            }
          }
        }
      }

      String output = String.join("\n", matches);
      OutputTruncator.TruncateResult truncation =
          OutputTruncator.truncate(output, request.context(), "fs-glob");

      Map<String, Object> structured = Map.of(
          "path", FsToolSupport.relativePath(request.context(), root),
          "pattern", pattern,
          "matches", List.copyOf(matches),
          "count", matches.size());

      Map<String, Object> metadata = new LinkedHashMap<>();
      metadata.put("truncated", truncation.truncated());
      metadata.put("limited", matches.size() >= MAX_RESULTS);
      if (truncation.outputPath() != null) {
        metadata.put("outputPath", truncation.outputPath().toString());
      }
      return ToolCallResult.success(truncation.content(), structured, Map.copyOf(metadata));
    } catch (IOException ex) {
      return ToolCallResult.error("FS_GLOB_ERROR", ex.getMessage());
    }
  }
}
