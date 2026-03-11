package cn.intentforge.prompt.local.plugin;

import cn.intentforge.prompt.model.PromptDefinition;
import cn.intentforge.prompt.spi.PromptPlugin;
import java.util.List;

public final class EmptyPromptPlugin implements PromptPlugin {
  @Override
  public List<PromptDefinition> prompts() {
    return List.of();
  }
}
