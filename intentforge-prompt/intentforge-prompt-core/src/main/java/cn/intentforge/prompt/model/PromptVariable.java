package cn.intentforge.prompt.model;

public record PromptVariable(
    String name,
    boolean required,
    String defaultValue,
    String description
) {
  public PromptVariable {
    name = requireText(name, "name");
    defaultValue = normalize(defaultValue);
    description = normalize(description);
  }

  private static String requireText(String value, String fieldName) {
    String normalized = normalize(value);
    if (normalized == null) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return normalized;
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
