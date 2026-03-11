package cn.intentforge.model.provider;

import cn.intentforge.model.catalog.ModelDescriptor;
import java.util.Collection;
import java.util.List;

public interface ModelProvider {
  ModelProviderDescriptor descriptor();

  default boolean available() {
    return true;
  }

  default Collection<ModelDescriptor> supportedModels() {
    return List.of();
  }
}
