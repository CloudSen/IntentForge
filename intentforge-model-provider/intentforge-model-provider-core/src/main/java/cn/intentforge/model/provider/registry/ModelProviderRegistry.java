package cn.intentforge.model.provider.registry;

import cn.intentforge.model.provider.ModelProvider;
import cn.intentforge.model.provider.ModelProviderDescriptor;
import cn.intentforge.model.registry.ModelManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ModelProviderRegistry {
  void register(ModelProvider modelProvider);

  void registerAll(Collection<? extends ModelProvider> modelProviders);

  void unregister(String id);

  void unregisterAll(Collection<? extends ModelProvider> modelProviders);

  Optional<ModelProvider> find(String id);

  List<ModelProviderDescriptor> list();

  void registerSupportedModels(ModelManager modelManager);

  void loadPlugins();
}
