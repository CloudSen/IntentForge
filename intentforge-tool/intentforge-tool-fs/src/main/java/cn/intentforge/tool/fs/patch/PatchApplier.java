package cn.intentforge.tool.fs.patch;

import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Applies parsed patch operations to workspace files.
 */
public final class PatchApplier {
  /**
   * Applies parsed patch operations to workspace files.
   *
   * @param context tool execution context
   * @param document patch document
   * @return changed file paths (workspace relative)
   */
  public List<String> apply(ToolExecutionContext context, PatchDocument document) {
    List<String> changedFiles = new ArrayList<>();
    for (PatchOperation operation : document.operations()) {
      Path targetPath = context.resolveWorkspacePath(operation.path());
      switch (operation.type()) {
        case ADD -> {
          writeText(targetPath, String.join("\n", operation.bodyLines()));
          changedFiles.add(relativePath(context, targetPath));
        }
        case DELETE -> {
          if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new IllegalArgumentException("Delete target does not exist: " + operation.path());
          }
          try {
            Files.delete(targetPath);
          } catch (Exception ex) {
            throw new IllegalStateException("Failed to delete file: " + targetPath, ex);
          }
          changedFiles.add(relativePath(context, targetPath));
        }
        case UPDATE -> {
          if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
            throw new IllegalArgumentException("Update target does not exist: " + operation.path());
          }
          String current = readText(targetPath);
          String updated = applyUpdateContent(current, operation.bodyLines());

          if (operation.moveTo() == null) {
            writeText(targetPath, updated);
            changedFiles.add(relativePath(context, targetPath));
          } else {
            Path moveTarget = context.resolveWorkspacePath(operation.moveTo());
            writeText(moveTarget, updated);
            try {
              Files.delete(targetPath);
            } catch (Exception ex) {
              throw new IllegalStateException("Failed to delete moved source: " + targetPath, ex);
            }
            changedFiles.add(relativePath(context, moveTarget));
          }
        }
      }
    }
    return List.copyOf(changedFiles);
  }

  private static String applyUpdateContent(String content, List<String> bodyLines) {
    if (bodyLines == null || bodyLines.isEmpty()) {
      return content;
    }

    List<List<String>> chunks = splitChunks(bodyLines);
    List<String> workingLines = new ArrayList<>(Arrays.asList(content.split("\n", -1)));
    int cursor = 0;

    for (List<String> chunk : chunks) {
      List<String> oldLines = new ArrayList<>();
      List<String> newLines = new ArrayList<>();
      for (String line : chunk) {
        if (line.isEmpty()) {
          throw new IllegalArgumentException("Invalid update line: empty line");
        }
        char marker = line.charAt(0);
        String value = line.substring(1);
        switch (marker) {
          case ' ' -> {
            oldLines.add(value);
            newLines.add(value);
          }
          case '-' -> oldLines.add(value);
          case '+' -> newLines.add(value);
          default -> throw new IllegalArgumentException("Invalid update line marker: " + line);
        }
      }

      int index = findSubList(workingLines, oldLines, cursor);
      if (index < 0) {
        throw new IllegalArgumentException("Failed to apply patch chunk; expected context not found");
      }
      int oldSize = oldLines.size();
      for (int i = 0; i < oldSize; i++) {
        workingLines.remove(index);
      }
      workingLines.addAll(index, newLines);
      cursor = index + newLines.size();
    }

    return String.join("\n", workingLines);
  }

  private static List<List<String>> splitChunks(List<String> bodyLines) {
    List<List<String>> chunks = new ArrayList<>();
    List<String> current = new ArrayList<>();
    for (String line : bodyLines) {
      if (line.startsWith("@@")) {
        if (!current.isEmpty()) {
          chunks.add(List.copyOf(current));
          current.clear();
        }
        continue;
      }
      current.add(line);
    }
    if (!current.isEmpty()) {
      chunks.add(List.copyOf(current));
    }
    if (chunks.isEmpty()) {
      chunks.add(List.copyOf(bodyLines));
    }
    return chunks;
  }

  private static int findSubList(List<String> source, List<String> target, int fromIndex) {
    if (target.isEmpty()) {
      return Math.max(0, fromIndex);
    }
    for (int index = Math.max(0, fromIndex); index <= source.size() - target.size(); index++) {
      boolean matched = true;
      for (int offset = 0; offset < target.size(); offset++) {
        if (!source.get(index + offset).equals(target.get(offset))) {
          matched = false;
          break;
        }
      }
      if (matched) {
        return index;
      }
    }
    return -1;
  }

  private static String readText(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read file: " + path, ex);
    }
  }

  private static void writeText(Path path, String content) {
    try {
      Path parent = path.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to write file: " + path, ex);
    }
  }

  private static String relativePath(ToolExecutionContext context, Path targetPath) {
    return context.workspaceRoot().relativize(targetPath).toString().replace('\\', '/');
  }
}
