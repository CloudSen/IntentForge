package cn.intentforge.boot.local;

import cn.intentforge.model.local.plugin.DirectoryModelPluginManager;
import cn.intentforge.model.local.registry.InMemoryModelManager;
import cn.intentforge.model.provider.local.plugin.DirectoryModelProviderPluginManager;
import cn.intentforge.model.provider.local.registry.InMemoryModelProviderRegistry;
import cn.intentforge.prompt.local.plugin.DirectoryPromptPluginManager;
import cn.intentforge.prompt.local.registry.InMemoryPromptManager;

public record AiAssetLocalRuntime(
    InMemoryPromptManager promptManager,
    DirectoryPromptPluginManager promptPluginManager,
    InMemoryModelManager modelManager,
    DirectoryModelPluginManager modelPluginManager,
    InMemoryModelProviderRegistry providerRegistry,
    DirectoryModelProviderPluginManager providerPluginManager
) {
}
