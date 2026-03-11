package cn.intentforge.model.local.plugin;

import cn.intentforge.model.catalog.ModelDescriptor;
import cn.intentforge.model.spi.ModelPlugin;
import java.util.List;

public final class EmptyModelPlugin implements ModelPlugin {
  @Override
  public List<ModelDescriptor> models() {
    return List.of();
  }
}
