package cn.intentforge.tool.core.util;

import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Utility that truncates large textual output and persists full content.
 */
public final class OutputTruncator {
  /**
   * Default max character count.
   */
  public static final int DEFAULT_MAX_CHARS = 20_000;

  private OutputTruncator() {
  }

  /**
   * Truncates output content when it exceeds {@link #DEFAULT_MAX_CHARS}.
   *
   * @param content content to process
   * @param context execution context
   * @param prefix output file prefix
   * @return truncate result
   */
  public static TruncateResult truncate(String content, ToolExecutionContext context, String prefix) {
    return truncate(content, DEFAULT_MAX_CHARS, context, prefix);
  }

  /**
   * Truncates output content and stores full content in output directory when truncated.
   *
   * @param content content to process
   * @param maxChars max characters to keep inline
   * @param context execution context
   * @param prefix output file prefix
   * @return truncate result
   */
  public static TruncateResult truncate(String content, int maxChars, ToolExecutionContext context, String prefix) {
    String safeContent = content == null ? "" : content;
    if (safeContent.length() <= maxChars) {
      return new TruncateResult(safeContent, false, null);
    }

    Path outputPath = context.toolOutputDirectory().resolve(buildFileName(prefix));
    try {
      Files.createDirectories(outputPath.getParent());
      Files.writeString(outputPath, safeContent, StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to write full tool output to " + outputPath, ex);
    }

    String truncatedContent = safeContent.substring(0, Math.max(0, maxChars))
        + "\n\n... output truncated ...\n"
        + "full_output_path=" + outputPath;
    return new TruncateResult(truncatedContent, true, outputPath);
  }

  private static String buildFileName(String prefix) {
    String safePrefix = prefix == null || prefix.isBlank() ? "tool-output" : prefix.trim();
    return safePrefix + "-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ".log";
  }

  /**
   * Result of truncation process.
   *
   * @param content output content used in tool response
   * @param truncated whether truncation happened
   * @param outputPath stored full output path when truncated
   */
  public record TruncateResult(String content, boolean truncated, Path outputPath) {
  }
}
