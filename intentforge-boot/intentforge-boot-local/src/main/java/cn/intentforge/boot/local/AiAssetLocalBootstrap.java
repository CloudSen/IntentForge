package cn.intentforge.boot.local;

import cn.intentforge.model.local.plugin.DirectoryModelPluginManager;
import cn.intentforge.model.local.registry.InMemoryModelManager;
import cn.intentforge.model.provider.local.plugin.DirectoryModelProviderPluginManager;
import cn.intentforge.model.provider.local.registry.InMemoryModelProviderRegistry;
import cn.intentforge.prompt.local.plugin.DirectoryPromptPluginManager;
import cn.intentforge.prompt.local.registry.InMemoryPromptManager;
import java.nio.file.Path;

public final class AiAssetLocalBootstrap {
  public static final Path DEFAULT_PLUGIN_DIRECTORY = Path.of("plugins");

  private AiAssetLocalBootstrap() {
  }

  public static AiAssetLocalRuntime bootstrap() {
    return bootstrap(DEFAULT_PLUGIN_DIRECTORY);
  }

  public static AiAssetLocalRuntime bootstrap(Path pluginsDirectory) {
    InMemoryPromptManager promptManager = InMemoryPromptManager.createAndLoad();
    DirectoryPromptPluginManager promptPluginManager =
        new DirectoryPromptPluginManager(pluginsDirectory, promptManager);
    promptPluginManager.loadAll();

    InMemoryModelManager modelManager = InMemoryModelManager.createAndLoad();
    DirectoryModelPluginManager modelPluginManager =
        new DirectoryModelPluginManager(pluginsDirectory, modelManager);
    modelPluginManager.loadAll();

    InMemoryModelProviderRegistry providerRegistry = InMemoryModelProviderRegistry.createAndLoad();
    DirectoryModelProviderPluginManager providerPluginManager =
        new DirectoryModelProviderPluginManager(pluginsDirectory, providerRegistry, modelManager);
    providerPluginManager.loadAll();

    return new AiAssetLocalRuntime(
        promptManager,
        promptPluginManager,
        modelManager,
        modelPluginManager,
        providerRegistry,
        providerPluginManager);
  }
}
