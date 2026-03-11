package cn.intentforge.prompt.local.plugin;

import cn.intentforge.common.plugin.DirectoryServicePluginLoader;
import cn.intentforge.common.plugin.PluginHandle;
import cn.intentforge.prompt.model.PromptDefinition;
import cn.intentforge.prompt.registry.PromptManager;
import cn.intentforge.prompt.spi.PromptPlugin;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DirectoryPromptPluginManager {
  public static final String PLUGIN_API_VERSION = "1";
  public static final String RUNTIME_VERSION = "nightly-SNAPSHOT";

  private final PromptManager promptManager;
  private final DirectoryServicePluginLoader<PromptPlugin> pluginLoader;
  private final Map<String, List<PromptDefinition>> promptDefinitionsByPluginId = new LinkedHashMap<>();

  public DirectoryPromptPluginManager(Path pluginsDirectory, PromptManager promptManager) {
    this(pluginsDirectory, promptManager, PLUGIN_API_VERSION, RUNTIME_VERSION);
  }

  public DirectoryPromptPluginManager(
      Path pluginsDirectory,
      PromptManager promptManager,
      String pluginApiVersion,
      String runtimeVersion
  ) {
    this.promptManager = promptManager;
    this.pluginLoader =
        new DirectoryServicePluginLoader<>(pluginsDirectory, PromptPlugin.class, pluginApiVersion, runtimeVersion);
  }

  public synchronized List<PluginHandle<PromptPlugin>> loadAll() {
    List<String> existingPluginIds = new ArrayList<>(promptDefinitionsByPluginId.keySet());
    List<PluginHandle<PromptPlugin>> handles = pluginLoader.loadAll();
    List<String> currentPluginIds = new ArrayList<>();
    for (PluginHandle<PromptPlugin> handle : handles) {
      currentPluginIds.add(handle.metadata().id());
      sync(handle);
    }
    for (String pluginId : existingPluginIds) {
      if (!currentPluginIds.contains(pluginId)) {
        unregister(pluginId);
      }
    }
    return handles;
  }

  public synchronized Optional<PluginHandle<PromptPlugin>> start(String pluginId) {
    Optional<PluginHandle<PromptPlugin>> handle = pluginLoader.start(pluginId);
    handle.ifPresent(this::sync);
    return handle;
  }

  public synchronized Optional<PluginHandle<PromptPlugin>> stop(String pluginId) {
    unregister(pluginId);
    return pluginLoader.stop(pluginId);
  }

  public synchronized List<PluginHandle<PromptPlugin>> plugins() {
    return pluginLoader.list();
  }

  private void sync(PluginHandle<PromptPlugin> handle) {
    unregister(handle.metadata().id());
    if (!handle.active()) {
      return;
    }
    List<PromptDefinition> promptDefinitions = handle.services()
        .stream()
        .map(PromptPlugin::prompts)
        .flatMap(definitions -> definitions.stream())
        .toList();
    promptManager.registerAll(promptDefinitions);
    promptDefinitionsByPluginId.put(handle.metadata().id(), promptDefinitions);
  }

  private void unregister(String pluginId) {
    List<PromptDefinition> promptDefinitions = promptDefinitionsByPluginId.remove(pluginId);
    if (promptDefinitions == null || promptDefinitions.isEmpty()) {
      return;
    }
    promptManager.unregisterAll(promptDefinitions);
  }
}
