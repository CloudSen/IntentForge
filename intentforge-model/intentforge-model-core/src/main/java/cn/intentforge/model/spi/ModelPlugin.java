package cn.intentforge.model.spi;

import cn.intentforge.model.catalog.ModelDescriptor;
import java.util.Collection;

public interface ModelPlugin {
  Collection<ModelDescriptor> models();
}
