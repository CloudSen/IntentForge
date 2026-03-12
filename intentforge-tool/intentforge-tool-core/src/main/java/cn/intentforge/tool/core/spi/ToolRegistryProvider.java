package cn.intentforge.tool.core.spi;

import cn.intentforge.tool.core.registry.ToolRegistry;

/**
 * SPI for supplying custom {@link ToolRegistry} implementation.
 */
public interface ToolRegistryProvider {
  /**
   * Priority used for provider selection. Higher value wins.
   *
   * @return provider priority
   */
  default int priority() {
    return 0;
  }

  /**
   * Creates a tool registry.
   *
   * @param classLoader class loader used for plugin lookup
   * @return created registry
   */
  ToolRegistry create(ClassLoader classLoader);
}
