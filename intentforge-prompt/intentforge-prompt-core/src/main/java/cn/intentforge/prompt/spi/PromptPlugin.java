package cn.intentforge.prompt.spi;

import cn.intentforge.prompt.model.PromptDefinition;
import java.util.Collection;

public interface PromptPlugin {
  Collection<PromptDefinition> prompts();
}
