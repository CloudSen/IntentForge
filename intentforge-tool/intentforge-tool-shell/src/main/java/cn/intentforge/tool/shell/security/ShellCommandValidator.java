package cn.intentforge.tool.shell.security;

import java.nio.file.Path;
import java.util.Set;

/**
 * Validates shell commands before execution.
 */
public interface ShellCommandValidator {
  /**
   * Validates command security rules.
   *
   * @param command shell command text
   * @param workingDirectory working directory
   * @param allowedExecutables optional executable whitelist
   * @return validation result
   */
  ValidationResult validate(String command, Path workingDirectory, Set<String> allowedExecutables);

  /**
   * Validation result.
   *
   * @param allowed whether command is allowed
   * @param reason reason when denied
   * @param executable parsed executable
   */
  record ValidationResult(boolean allowed, String reason, String executable) {
    /**
     * Builds allowed result.
     *
     * @param executable executable name
     * @return allowed result
     */
    public static ValidationResult allowed(String executable) {
      return new ValidationResult(true, "allowed", executable);
    }

    /**
     * Builds denied result.
     *
     * @param reason deny reason
     * @param executable executable name
     * @return denied result
     */
    public static ValidationResult denied(String reason, String executable) {
      return new ValidationResult(false, reason, executable);
    }
  }
}
