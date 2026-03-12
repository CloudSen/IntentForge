package cn.intentforge.tool.core.local.plugin;

import cn.intentforge.common.plugin.DirectoryServicePluginLoader;
import cn.intentforge.common.plugin.PluginHandle;
import cn.intentforge.tool.core.registry.ToolRegistration;
import cn.intentforge.tool.core.registry.ToolRegistry;
import cn.intentforge.tool.core.spi.ToolPlugin;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Plugin manager that loads {@link ToolPlugin} implementations from plugin jars.
 */
public final class DirectoryToolPluginManager {
  /**
   * Tool plugin API version.
   */
  public static final String PLUGIN_API_VERSION = "1";
  /**
   * Runtime version used by compatibility validation.
   */
  public static final String RUNTIME_VERSION = "nightly-SNAPSHOT";

  private final ToolRegistry toolRegistry;
  private final DirectoryServicePluginLoader<ToolPlugin> pluginLoader;
  private final Map<String, Set<String>> toolIdsByPluginId = new LinkedHashMap<>();

  /**
   * Creates manager with default API/runtime versions.
   *
   * @param pluginsDirectory plugin directory
   * @param toolRegistry tool registry
   */
  public DirectoryToolPluginManager(Path pluginsDirectory, ToolRegistry toolRegistry) {
    this(pluginsDirectory, toolRegistry, PLUGIN_API_VERSION, RUNTIME_VERSION);
  }

  /**
   * Creates manager with explicit API/runtime versions.
   *
   * @param pluginsDirectory plugin directory
   * @param toolRegistry tool registry
   * @param pluginApiVersion plugin api version
   * @param runtimeVersion runtime version
   */
  public DirectoryToolPluginManager(
      Path pluginsDirectory,
      ToolRegistry toolRegistry,
      String pluginApiVersion,
      String runtimeVersion
  ) {
    this.toolRegistry = toolRegistry;
    this.pluginLoader = new DirectoryServicePluginLoader<>(
        pluginsDirectory,
        ToolPlugin.class,
        pluginApiVersion,
        runtimeVersion);
  }

  /**
   * Loads all plugins and syncs their tools.
   *
   * @return plugin handles
   */
  public synchronized List<PluginHandle<ToolPlugin>> loadAll() {
    List<String> existingPluginIds = new ArrayList<>(toolIdsByPluginId.keySet());
    List<PluginHandle<ToolPlugin>> handles = pluginLoader.loadAll();
    List<String> currentPluginIds = new ArrayList<>();
    for (PluginHandle<ToolPlugin> handle : handles) {
      String pluginId = handle.metadata().id();
      currentPluginIds.add(pluginId);
      sync(handle);
    }
    for (String pluginId : existingPluginIds) {
      if (!currentPluginIds.contains(pluginId)) {
        unregister(pluginId);
      }
    }
    return handles;
  }

  /**
   * Starts one plugin and syncs its tools.
   *
   * @param pluginId plugin id
   * @return plugin handle optional
   */
  public synchronized Optional<PluginHandle<ToolPlugin>> start(String pluginId) {
    Optional<PluginHandle<ToolPlugin>> handle = pluginLoader.start(pluginId);
    handle.ifPresent(this::sync);
    return handle;
  }

  /**
   * Stops one plugin and unregisters contributed tools.
   *
   * @param pluginId plugin id
   * @return plugin handle optional
   */
  public synchronized Optional<PluginHandle<ToolPlugin>> stop(String pluginId) {
    unregister(pluginId);
    return pluginLoader.stop(pluginId);
  }

  /**
   * Lists plugin handles.
   *
   * @return plugin handles
   */
  public synchronized List<PluginHandle<ToolPlugin>> plugins() {
    return pluginLoader.list();
  }

  private void sync(PluginHandle<ToolPlugin> handle) {
    String pluginId = handle.metadata().id();
    unregister(pluginId);
    if (!handle.active()) {
      return;
    }
    Set<String> toolIds = new LinkedHashSet<>();
    List<ToolRegistration> registrations = handle.services()
        .stream()
        .map(ToolPlugin::tools)
        .filter(collection -> collection != null && !collection.isEmpty())
        .flatMap(collection -> collection.stream())
        .toList();
    for (ToolRegistration registration : registrations) {
      toolRegistry.register(registration);
      toolIds.add(registration.definition().id());
    }
    toolIdsByPluginId.put(pluginId, toolIds);
  }

  private void unregister(String pluginId) {
    Set<String> toolIds = toolIdsByPluginId.remove(pluginId);
    if (toolIds == null || toolIds.isEmpty()) {
      return;
    }
    for (String toolId : toolIds) {
      toolRegistry.unregister(toolId);
    }
  }
}
