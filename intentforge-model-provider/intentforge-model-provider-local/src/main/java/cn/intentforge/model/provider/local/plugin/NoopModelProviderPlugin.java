package cn.intentforge.model.provider.local.plugin;

import cn.intentforge.model.provider.ModelProvider;
import cn.intentforge.model.provider.ModelProviderDescriptor;
import cn.intentforge.model.provider.ModelProviderType;
import cn.intentforge.model.provider.spi.ModelProviderPlugin;
import java.util.List;
import java.util.Map;

public final class NoopModelProviderPlugin implements ModelProviderPlugin {
  @Override
  public ModelProvider provider() {
    return new ModelProvider() {
      @Override
      public ModelProviderDescriptor descriptor() {
        return new ModelProviderDescriptor(
            "intentforge-noop-provider",
            "IntentForge Noop Provider",
            "Built-in noop provider placeholder.",
            ModelProviderType.CUSTOM,
            "noop://provider",
            List.of(),
            Map.of("builtin", "true"));
      }

      @Override
      public boolean available() {
        return false;
      }
    };
  }
}
