package cn.intentforge.model.registry;

import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.catalog.ModelQuery;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ModelManager {
  void register(ModelDescriptor modelDescriptor);

  void registerAll(Collection<ModelDescriptor> modelDescriptors);

  void unregister(String id);

  void unregisterAll(Collection<ModelDescriptor> modelDescriptors);

  Optional<ModelDescriptor> find(String id);

  List<ModelDescriptor> list(ModelQuery query);

  void loadPlugins();
}
