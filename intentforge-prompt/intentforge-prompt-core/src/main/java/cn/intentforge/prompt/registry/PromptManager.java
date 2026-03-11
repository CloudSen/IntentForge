package cn.intentforge.prompt.registry;

import cn.intentforge.prompt.model.PromptDefinition;
import cn.intentforge.prompt.model.PromptQuery;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PromptManager {
  void register(PromptDefinition promptDefinition);

  void registerAll(Collection<PromptDefinition> promptDefinitions);

  void unregister(String id, String version);

  void unregisterAll(Collection<PromptDefinition> promptDefinitions);

  Optional<PromptDefinition> find(String id, String version);

  Optional<PromptDefinition> findLatest(String id);

  List<PromptDefinition> list(PromptQuery query);

  void loadPlugins();
}
