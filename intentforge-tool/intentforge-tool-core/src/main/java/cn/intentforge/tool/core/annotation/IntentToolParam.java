package cn.intentforge.tool.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares one parameter used by an annotation-based tool method.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IntentToolParam {
  /**
   * Parameter name in input map.
   *
   * @return parameter name
   */
  String name();

  /**
   * Whether this parameter is required.
   *
   * @return required flag
   */
  boolean required() default true;

  /**
   * Optional parameter description.
   *
   * @return description
   */
  String description() default "";

  /**
   * Optional explicit schema type.
   *
   * @return schema type
   */
  String type() default "";
}
