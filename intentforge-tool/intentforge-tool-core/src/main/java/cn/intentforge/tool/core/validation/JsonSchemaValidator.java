package cn.intentforge.tool.core.validation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Lightweight JSON-schema-like validator used for tool parameters.
 */
public final class JsonSchemaValidator {
  /**
   * Validates input against schema.
   *
   * @param schema schema map
   * @param input input map
   * @return null when valid, otherwise validation error message
   */
  public String validate(Map<String, Object> schema, Map<String, Object> input) {
    if (schema == null || schema.isEmpty()) {
      return null;
    }
    if (input == null) {
      return "input must not be null";
    }
    Object result = validateValue("$", input, schema);
    return result == null ? null : String.valueOf(result);
  }

  private Object validateValue(String path, Object value, Map<String, Object> schema) {
    String type = asString(schema.get("type"));
    if (type != null && !matchesType(type, value)) {
      return path + " should be " + type;
    }

    Object enumValues = schema.get("enum");
    if (enumValues instanceof List<?> options && !options.isEmpty() && !options.contains(value)) {
      return path + " should be one of " + options;
    }

    if (value instanceof String textValue) {
      Object minLength = schema.get("minLength");
      if (minLength instanceof Number min && textValue.length() < min.intValue()) {
        return path + " length should be >= " + min.intValue();
      }
      Object maxLength = schema.get("maxLength");
      if (maxLength instanceof Number max && textValue.length() > max.intValue()) {
        return path + " length should be <= " + max.intValue();
      }
      String pattern = asString(schema.get("pattern"));
      if (pattern != null) {
        try {
          if (!Pattern.compile(pattern).matcher(textValue).find()) {
            return path + " does not match pattern " + pattern;
          }
        } catch (PatternSyntaxException ex) {
          return "Invalid schema pattern at " + path + ": " + ex.getMessage();
        }
      }
    }

    if (value instanceof Number numberValue) {
      Object minimum = schema.get("minimum");
      if (minimum instanceof Number min && numberValue.doubleValue() < min.doubleValue()) {
        return path + " should be >= " + min.doubleValue();
      }
      Object maximum = schema.get("maximum");
      if (maximum instanceof Number max && numberValue.doubleValue() > max.doubleValue()) {
        return path + " should be <= " + max.doubleValue();
      }
    }

    if (value instanceof Map<?, ?> objectValue) {
      Object requiredValue = schema.get("required");
      if (requiredValue instanceof List<?> requiredFields) {
        for (Object field : requiredFields) {
          String fieldName = asString(field);
          if (fieldName == null) {
            continue;
          }
          if (!objectValue.containsKey(fieldName)) {
            return path + "." + fieldName + " is required";
          }
        }
      }

      Object propertiesValue = schema.get("properties");
      if (propertiesValue instanceof Map<?, ?> properties) {
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
          String fieldName = asString(entry.getKey());
          if (fieldName == null || !objectValue.containsKey(fieldName)) {
            continue;
          }
          Object fieldSchemaValue = entry.getValue();
          if (!(fieldSchemaValue instanceof Map<?, ?> rawFieldSchema)) {
            continue;
          }
          @SuppressWarnings("unchecked")
          Map<String, Object> fieldSchema = (Map<String, Object>) rawFieldSchema;
          Object error = validateValue(path + "." + fieldName, objectValue.get(fieldName), fieldSchema);
          if (error != null) {
            return error;
          }
        }
      }
    }

    if (value instanceof List<?> listValue) {
      Object itemsValue = schema.get("items");
      if (itemsValue instanceof Map<?, ?> rawItemSchema) {
        @SuppressWarnings("unchecked")
        Map<String, Object> itemSchema = (Map<String, Object>) rawItemSchema;
        for (int index = 0; index < listValue.size(); index++) {
          Object error = validateValue(path + "[" + index + "]", listValue.get(index), itemSchema);
          if (error != null) {
            return error;
          }
        }
      }
    }

    return null;
  }

  private boolean matchesType(String type, Object value) {
    return switch (type) {
      case "string" -> value instanceof String;
      case "number" -> value instanceof Number;
      case "integer" -> value instanceof Integer || value instanceof Long;
      case "boolean" -> value instanceof Boolean;
      case "object" -> value instanceof Map<?, ?>;
      case "array" -> value instanceof List<?>;
      default -> true;
    };
  }

  private static String asString(Object value) {
    if (value == null) {
      return null;
    }
    String text = Objects.toString(value, null);
    if (text == null) {
      return null;
    }
    String normalized = text.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
