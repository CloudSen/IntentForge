package cn.intentforge.tool.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks one method as tool entrypoint for annotation-based registration.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IntentTool {
  /**
   * Tool identifier. If blank, method name is used.
   *
   * @return tool identifier
   */
  String id() default "";

  /**
   * Tool description.
   *
   * @return description text
   */
  String description() default "";

  /**
   * Whether this tool is sensitive.
   *
   * @return true for sensitive tool
   */
  boolean sensitive() default false;
}
