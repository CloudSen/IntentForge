package cn.intentforge.tool.core.spi;

import cn.intentforge.tool.core.registry.ToolRegistration;
import java.util.Collection;

/**
 * SPI for providing tool registrations.
 */
public interface ToolPlugin {
  /**
   * Returns tools contributed by this plugin.
   *
   * @return tool registrations
   */
  Collection<ToolRegistration> tools();
}
