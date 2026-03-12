package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

/**
 * Shared helper utilities for filesystem tools.
 */
final class FsToolSupport {
  private FsToolSupport() {
  }

  static Path resolvePath(ToolExecutionContext context, Map<String, Object> parameters, String key, boolean required) {
    String value = readString(parameters, key);
    if (value == null) {
      if (required) {
        throw new IllegalArgumentException(key + " is required");
      }
      return context.workspaceRoot();
    }
    return context.resolveWorkspacePath(value);
  }

  static String readString(Map<String, Object> parameters, String key) {
    Object value = parameters.get(key);
    if (value == null) {
      return null;
    }
    String normalized = String.valueOf(value).trim();
    return normalized.isEmpty() ? null : normalized;
  }

  static int readInt(Map<String, Object> parameters, String key, int defaultValue) {
    Object value = parameters.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(String.valueOf(value).trim());
  }

  static boolean readBoolean(Map<String, Object> parameters, String key, boolean defaultValue) {
    Object value = parameters.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return Boolean.parseBoolean(String.valueOf(value).trim().toLowerCase(Locale.ROOT));
  }

  static String relativePath(ToolExecutionContext context, Path targetPath) {
    return context.workspaceRoot().relativize(targetPath).toString().replace('\\', '/');
  }

  static boolean isBinaryFile(Path path) {
    try (InputStream stream = Files.newInputStream(path)) {
      byte[] buffer = stream.readNBytes(2048);
      if (buffer.length == 0) {
        return false;
      }
      int nonPrintable = 0;
      for (byte value : buffer) {
        if (value == 0) {
          return true;
        }
        int unsigned = value & 0xFF;
        if (unsigned < 9 || (unsigned > 13 && unsigned < 32)) {
          nonPrintable++;
        }
      }
      return (double) nonPrintable / (double) buffer.length > 0.30;
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to inspect file type: " + path, ex);
    }
  }

  static String readText(Path path) {
    try {
      return Files.readString(path, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read file: " + path, ex);
    }
  }

  static void writeText(Path path, String content) {
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
}
