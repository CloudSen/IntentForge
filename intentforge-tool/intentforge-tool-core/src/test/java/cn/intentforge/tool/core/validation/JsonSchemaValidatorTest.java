package cn.intentforge.tool.core.validation;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonSchemaValidatorTest {
  private final JsonSchemaValidator validator = new JsonSchemaValidator();

  @Test
  void shouldValidateRequiredAndType() {
    Map<String, Object> schema = Map.of(
        "type", "object",
        "properties", Map.of(
            "name", Map.of("type", "string"),
            "age", Map.of("type", "integer", "minimum", 0)
        ),
        "required", List.of("name", "age")
    );

    String valid = validator.validate(schema, Map.of("name", "alice", "age", 18));
    Assertions.assertNull(valid);

    String missing = validator.validate(schema, Map.of("name", "alice"));
    Assertions.assertNotNull(missing);
    Assertions.assertTrue(missing.contains("age"));

    String wrongType = validator.validate(schema, Map.of("name", "alice", "age", "bad"));
    Assertions.assertNotNull(wrongType);
    Assertions.assertTrue(wrongType.contains("integer"));
  }

  @Test
  void shouldValidateNestedAndArrayItems() {
    Map<String, Object> schema = Map.of(
        "type", "object",
        "properties", Map.of(
            "tags", Map.of(
                "type", "array",
                "items", Map.of("type", "string", "minLength", 2)
            ),
            "meta", Map.of(
                "type", "object",
                "properties", Map.of(
                    "enabled", Map.of("type", "boolean")
                ),
                "required", List.of("enabled")
            )
        ),
        "required", List.of("tags", "meta")
    );

    String valid = validator.validate(schema, Map.of(
        "tags", List.of("aa", "bb"),
        "meta", Map.of("enabled", true)
    ));
    Assertions.assertNull(valid);

    String invalidItem = validator.validate(schema, Map.of(
        "tags", List.of("a"),
        "meta", Map.of("enabled", true)
    ));
    Assertions.assertNotNull(invalidItem);
    Assertions.assertTrue(invalidItem.contains("length"));
  }
}
