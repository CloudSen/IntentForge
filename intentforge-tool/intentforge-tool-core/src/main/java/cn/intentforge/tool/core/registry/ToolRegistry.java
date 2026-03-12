package cn.intentforge.tool.core.registry;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Registry for tool definitions and handlers.
 */
public interface ToolRegistry {
  /**
   * Registers a tool.
   *
   * @param registration registration to add
   */
  void register(ToolRegistration registration);

  /**
   * Registers multiple tools.
   *
   * @param registrations registrations to add
   */
  void registerAll(Collection<ToolRegistration> registrations);

  /**
   * Unregisters one tool.
   *
   * @param toolId tool identifier
   */
  void unregister(String toolId);

  /**
   * Finds one tool.
   *
   * @param toolId tool identifier
   * @return optional registration
   */
  Optional<ToolRegistration> find(String toolId);

  /**
   * Lists all registered tools.
   *
   * @return immutable registration list
   */
  List<ToolRegistration> list();

  /**
   * Loads classpath tool plugins.
   */
  void loadPlugins();
}
