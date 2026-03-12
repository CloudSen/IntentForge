package cn.intentforge.agent.nativejava;

import cn.intentforge.agent.core.ContextPack;
import cn.intentforge.tool.core.model.ToolDefinition;

final class NativeAgentSupport {
  private NativeAgentSupport() {
  }

  static String firstPromptId(ContextPack contextPack) {
    return contextPack.prompts().isEmpty() ? "unbound" : contextPack.prompts().getFirst().id();
  }

  static String firstModelId(ContextPack contextPack) {
    return contextPack.models().isEmpty() ? "unbound" : contextPack.models().getFirst().id();
  }

  static String firstProviderId(ContextPack contextPack) {
    return contextPack.modelProviders().isEmpty() ? "unbound" : contextPack.modelProviders().getFirst().id();
  }

  static boolean hasTool(ContextPack contextPack, String toolId) {
    for (ToolDefinition tool : contextPack.tools()) {
      if (tool.id().equals(toolId)) {
        return true;
      }
    }
    return false;
  }

  static String preferredWorkspaceTool(ContextPack contextPack) {
    if (hasTool(contextPack, "intentforge.fs.list")) {
      return "intentforge.fs.list";
    }
    return contextPack.tools().isEmpty() ? null : contextPack.tools().getFirst().id();
  }
}
