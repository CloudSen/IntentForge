package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Handler for {@code intentforge.fs.edit}.
 */
final class FsEditToolHandler implements ToolHandler {
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      Path path = FsToolSupport.resolvePath(request.context(), request.parameters(), "path", true);
      if (!Files.exists(path) || !Files.isRegularFile(path)) {
        return ToolCallResult.error("FS_NOT_FILE", "Path is not a regular file: " + path);
      }

      String oldString = FsToolSupport.readString(request.parameters(), "oldString");
      String newString = FsToolSupport.readString(request.parameters(), "newString");
      boolean replaceAll = FsToolSupport.readBoolean(request.parameters(), "replaceAll", false);

      if (oldString == null) {
        return ToolCallResult.error("FS_INVALID_ARGUMENT", "oldString is required");
      }
      if (newString == null) {
        return ToolCallResult.error("FS_INVALID_ARGUMENT", "newString is required");
      }
      if (oldString.equals(newString)) {
        return ToolCallResult.error("FS_INVALID_ARGUMENT", "oldString and newString are identical");
      }

      String content = FsToolSupport.readText(path);
      int occurrences = countOccurrences(content, oldString);
      if (occurrences == 0) {
        return ToolCallResult.error("FS_EDIT_NOT_FOUND", "oldString not found in file");
      }
      if (!replaceAll && occurrences > 1) {
        return ToolCallResult.error("FS_EDIT_AMBIGUOUS", "oldString has multiple matches; set replaceAll=true");
      }

      String updated;
      if (replaceAll) {
        updated = content.replace(oldString, newString);
      } else {
        int index = content.indexOf(oldString);
        updated = content.substring(0, index) + newString + content.substring(index + oldString.length());
      }
      FsToolSupport.writeText(path, updated);

      return ToolCallResult.success(
          "Edited file: " + FsToolSupport.relativePath(request.context(), path),
          Map.of(
              "path", FsToolSupport.relativePath(request.context(), path),
              "occurrences", occurrences,
              "replaceAll", replaceAll),
          Map.of());
    } catch (Exception ex) {
      return ToolCallResult.error("FS_EDIT_ERROR", ex.getMessage());
    }
  }

  private static int countOccurrences(String text, String needle) {
    int count = 0;
    int start = 0;
    while (start >= 0) {
      int index = text.indexOf(needle, start);
      if (index < 0) {
        break;
      }
      count++;
      start = index + needle.length();
    }
    return count;
  }
}
