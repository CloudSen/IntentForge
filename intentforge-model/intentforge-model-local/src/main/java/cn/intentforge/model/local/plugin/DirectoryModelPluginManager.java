package cn.intentforge.model.local.plugin;

import cn.intentforge.common.plugin.DirectoryServicePluginLoader;
import cn.intentforge.common.plugin.PluginHandle;
import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.registry.ModelManager;
import cn.intentforge.model.spi.ModelPlugin;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DirectoryModelPluginManager {
  public static final String PLUGIN_API_VERSION = "1";
  public static final String RUNTIME_VERSION = "nightly-SNAPSHOT";

  private final ModelManager modelManager;
  private final DirectoryServicePluginLoader<ModelPlugin> pluginLoader;
  private final Map<String, List<ModelDescriptor>> modelDescriptorsByPluginId = new LinkedHashMap<>();

  public DirectoryModelPluginManager(Path pluginsDirectory, ModelManager modelManager) {
    this(pluginsDirectory, modelManager, PLUGIN_API_VERSION, RUNTIME_VERSION);
  }

  public DirectoryModelPluginManager(
      Path pluginsDirectory,
      ModelManager modelManager,
      String pluginApiVersion,
      String runtimeVersion
  ) {
    this.modelManager = modelManager;
    this.pluginLoader =
        new DirectoryServicePluginLoader<>(pluginsDirectory, ModelPlugin.class, pluginApiVersion, runtimeVersion);
  }

  public synchronized List<PluginHandle<ModelPlugin>> loadAll() {
    List<String> existingPluginIds = new ArrayList<>(modelDescriptorsByPluginId.keySet());
    List<PluginHandle<ModelPlugin>> handles = pluginLoader.loadAll();
    List<String> currentPluginIds = new ArrayList<>();
    for (PluginHandle<ModelPlugin> handle : handles) {
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

  public synchronized Optional<PluginHandle<ModelPlugin>> start(String pluginId) {
    Optional<PluginHandle<ModelPlugin>> handle = pluginLoader.start(pluginId);
    handle.ifPresent(this::sync);
    return handle;
  }

  public synchronized Optional<PluginHandle<ModelPlugin>> stop(String pluginId) {
    unregister(pluginId);
    return pluginLoader.stop(pluginId);
  }

  public synchronized List<PluginHandle<ModelPlugin>> plugins() {
    return pluginLoader.list();
  }

  private void sync(PluginHandle<ModelPlugin> handle) {
    unregister(handle.metadata().id());
    if (!handle.active()) {
      return;
    }
    List<ModelDescriptor> modelDescriptors = handle.services()
        .stream()
        .map(ModelPlugin::models)
        .flatMap(models -> models.stream())
        .toList();
    modelManager.registerAll(modelDescriptors);
    modelDescriptorsByPluginId.put(handle.metadata().id(), modelDescriptors);
  }

  private void unregister(String pluginId) {
    List<ModelDescriptor> modelDescriptors = modelDescriptorsByPluginId.remove(pluginId);
    if (modelDescriptors == null || modelDescriptors.isEmpty()) {
      return;
    }
    modelManager.unregisterAll(modelDescriptors);
  }
}
