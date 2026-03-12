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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Handler for {@code intentforge.fs.grep}.
 */
final class FsGrepToolHandler implements ToolHandler {
  private static final int MAX_MATCHES = 200;

  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      String patternText = FsToolSupport.readString(request.parameters(), "pattern");
      if (patternText == null) {
        return ToolCallResult.error("FS_INVALID_ARGUMENT", "pattern is required");
      }
      Pattern pattern;
      try {
        pattern = Pattern.compile(patternText);
      } catch (PatternSyntaxException ex) {
        return ToolCallResult.error("FS_INVALID_REGEX", ex.getMessage());
      }

      String includePattern = FsToolSupport.readString(request.parameters(), "include");
      PathMatcher includeMatcher = includePattern == null
          ? null
          : FileSystems.getDefault().getPathMatcher("glob:" + includePattern);

      Path root = FsToolSupport.resolvePath(request.context(), request.parameters(), "path", false);
      if (!Files.exists(root) || !Files.isDirectory(root)) {
        return ToolCallResult.error("FS_NOT_DIRECTORY", "Search path is not directory: " + root);
      }

      List<String> matches = new ArrayList<>();
      try (var stream = Files.walk(root)) {
        for (Path file : stream.toList()) {
          if (!Files.isRegularFile(file)) {
            continue;
          }
          Path relative = root.relativize(file);
          if (includeMatcher != null && !includeMatcher.matches(relative)) {
            continue;
          }
          if (FsToolSupport.isBinaryFile(file)) {
            continue;
          }

          List<String> lines = Files.readAllLines(file);
          for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            if (!pattern.matcher(line).find()) {
              continue;
            }
            matches.add(
                FsToolSupport.relativePath(request.context(), file)
                    + ":"
                    + (lineIndex + 1)
                    + ":"
                    + line);
            if (matches.size() >= MAX_MATCHES) {
              break;
            }
          }
          if (matches.size() >= MAX_MATCHES) {
            break;
          }
        }
      }

      String output = matches.isEmpty() ? "No matches" : String.join("\n", matches);
      OutputTruncator.TruncateResult truncation =
          OutputTruncator.truncate(output, request.context(), "fs-grep");

      Map<String, Object> structured = new LinkedHashMap<>();
      structured.put("pattern", patternText);
      structured.put("path", FsToolSupport.relativePath(request.context(), root));
      structured.put("matches", List.copyOf(matches));
      structured.put("count", matches.size());

      Map<String, Object> metadata = new LinkedHashMap<>();
      metadata.put("truncated", truncation.truncated());
      metadata.put("limited", matches.size() >= MAX_MATCHES);
      if (truncation.outputPath() != null) {
        metadata.put("outputPath", truncation.outputPath().toString());
      }

      return ToolCallResult.success(truncation.content(), Map.copyOf(structured), Map.copyOf(metadata));
    } catch (IOException ex) {
      return ToolCallResult.error("FS_GREP_ERROR", ex.getMessage());
    }
  }
}
