package cn.intentforge.agent.core;

/**
 * Raised when routing or execution cannot continue.
 */
public class AgentExecutionException extends RuntimeException {
  /**
   * Creates an exception with a message.
   *
   * @param message error message
   */
  public AgentExecutionException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message and cause.
   *
   * @param message error message
   * @param cause root cause
   */
  public AgentExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
