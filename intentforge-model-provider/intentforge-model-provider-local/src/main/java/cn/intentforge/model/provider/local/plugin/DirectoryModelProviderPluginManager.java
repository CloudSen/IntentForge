package cn.intentforge.model.provider.local.plugin;

import cn.intentforge.common.plugin.DirectoryServicePluginLoader;
import cn.intentforge.common.plugin.PluginHandle;
import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.provider.ModelProvider;
import cn.intentforge.model.provider.registry.ModelProviderRegistry;
import cn.intentforge.model.provider.spi.ModelProviderPlugin;
import cn.intentforge.model.registry.ModelManager;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DirectoryModelProviderPluginManager {
  public static final String PLUGIN_API_VERSION = "1";
  public static final String RUNTIME_VERSION = "nightly-SNAPSHOT";

  private final ModelProviderRegistry providerRegistry;
  private final ModelManager modelManager;
  private final DirectoryServicePluginLoader<ModelProviderPlugin> pluginLoader;
  private final Map<String, List<ModelProvider>> providersByPluginId = new LinkedHashMap<>();
  private final Map<String, List<ModelDescriptor>> modelDescriptorsByPluginId = new LinkedHashMap<>();

  public DirectoryModelProviderPluginManager(Path pluginsDirectory, ModelProviderRegistry providerRegistry) {
    this(pluginsDirectory, providerRegistry, null, PLUGIN_API_VERSION, RUNTIME_VERSION);
  }

  public DirectoryModelProviderPluginManager(
      Path pluginsDirectory,
      ModelProviderRegistry providerRegistry,
      ModelManager modelManager
  ) {
    this(pluginsDirectory, providerRegistry, modelManager, PLUGIN_API_VERSION, RUNTIME_VERSION);
  }

  public DirectoryModelProviderPluginManager(
      Path pluginsDirectory,
      ModelProviderRegistry providerRegistry,
      ModelManager modelManager,
      String pluginApiVersion,
      String runtimeVersion
  ) {
    this.providerRegistry = providerRegistry;
    this.modelManager = modelManager;
    this.pluginLoader = new DirectoryServicePluginLoader<>(
        pluginsDirectory,
        ModelProviderPlugin.class,
        pluginApiVersion,
        runtimeVersion);
  }

  public synchronized List<PluginHandle<ModelProviderPlugin>> loadAll() {
    List<String> existingPluginIds = new ArrayList<>(providersByPluginId.keySet());
    List<PluginHandle<ModelProviderPlugin>> handles = pluginLoader.loadAll();
    List<String> currentPluginIds = new ArrayList<>();
    for (PluginHandle<ModelProviderPlugin> handle : handles) {
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

  public synchronized Optional<PluginHandle<ModelProviderPlugin>> start(String pluginId) {
    Optional<PluginHandle<ModelProviderPlugin>> handle = pluginLoader.start(pluginId);
    handle.ifPresent(this::sync);
    return handle;
  }

  public synchronized Optional<PluginHandle<ModelProviderPlugin>> stop(String pluginId) {
    unregister(pluginId);
    return pluginLoader.stop(pluginId);
  }

  public synchronized List<PluginHandle<ModelProviderPlugin>> plugins() {
    return pluginLoader.list();
  }

  private void sync(PluginHandle<ModelProviderPlugin> handle) {
    unregister(handle.metadata().id());
    if (!handle.active()) {
      return;
    }

    List<ModelProvider> providers = handle.services()
        .stream()
        .map(ModelProviderPlugin::provider)
        .filter(ModelProvider::available)
        .toList();
    providerRegistry.registerAll(providers);
    providersByPluginId.put(handle.metadata().id(), providers);

    if (modelManager == null) {
      return;
    }
    List<ModelDescriptor> modelDescriptors = providers.stream()
        .map(ModelProvider::supportedModels)
        .flatMap(models -> models.stream())
        .toList();
    modelManager.registerAll(modelDescriptors);
    modelDescriptorsByPluginId.put(handle.metadata().id(), modelDescriptors);
  }

  private void unregister(String pluginId) {
    List<ModelDescriptor> modelDescriptors = modelDescriptorsByPluginId.remove(pluginId);
    if (modelDescriptors != null && !modelDescriptors.isEmpty() && modelManager != null) {
      modelManager.unregisterAll(modelDescriptors);
    }

    List<ModelProvider> providers = providersByPluginId.remove(pluginId);
    if (providers == null || providers.isEmpty()) {
      return;
    }
    providerRegistry.unregisterAll(providers);
  }
}
