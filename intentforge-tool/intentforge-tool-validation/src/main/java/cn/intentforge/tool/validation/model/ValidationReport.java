package cn.intentforge.tool.validation.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified report for build/test/lint validation tools.
 *
 * @param success whether validation passed
 * @param failedChecks failed check descriptions
 * @param logExcerpt log excerpt for diagnostics
 * @param durationMs execution duration in milliseconds
 */
public record ValidationReport(
    boolean success,
    List<String> failedChecks,
    String logExcerpt,
    long durationMs
) {
  /**
   * Creates a validation report.
   *
   * @param success whether validation passed
   * @param failedChecks failed check descriptions
   * @param logExcerpt log excerpt for diagnostics
   * @param durationMs execution duration in milliseconds
   */
  public ValidationReport {
    failedChecks = failedChecks == null ? List.of() : List.copyOf(failedChecks);
    logExcerpt = logExcerpt == null ? "" : logExcerpt;
    durationMs = Math.max(0, durationMs);
  }

  /**
   * Converts report into immutable map.
   *
   * @return report map
   */
  public Map<String, Object> toMap() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("success", success);
    payload.put("failedChecks", failedChecks);
    payload.put("logExcerpt", logExcerpt);
    payload.put("duration", durationMs);
    return Map.copyOf(payload);
  }
}
