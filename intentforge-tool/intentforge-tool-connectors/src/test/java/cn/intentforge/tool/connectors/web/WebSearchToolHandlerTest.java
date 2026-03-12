package cn.intentforge.tool.connectors.web;

import cn.intentforge.tool.connectors.ConnectorToolPlugin;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebSearchToolHandlerTest {
  @Test
  void shouldSwitchProvider() throws Exception {
    SearchProvider providerA = new FixedProvider("alpha", List.of(new SearchResultItem("A", "https://a", "A-snippet")));
    SearchProvider providerB = new FixedProvider("beta", List.of(new SearchResultItem("B", "https://b", "B-snippet")));
    WebSearchToolHandler handler = new WebSearchToolHandler(List.of(providerA, providerB), "alpha");
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("web-search-provider"));

    var result = handler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_WEB_SEARCH,
        Map.of("q", "hello", "provider", "beta"),
        context));
    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertEquals("beta", result.metadata().get("provider"));
    Assertions.assertEquals(Boolean.FALSE, result.metadata().get("fallbackUsed"));
  }

  @Test
  void shouldFallbackWhenProviderFailed() throws Exception {
    SearchProvider failed = new SearchProvider() {
      @Override
      public String id() {
        return "failed";
      }

      @Override
      public List<SearchResultItem> search(String query, int limit, Duration timeout) {
        throw new IllegalStateException("boom");
      }
    };
    SearchProvider fallback = new FixedProvider("fallback", List.of(new SearchResultItem("F", "https://f", "F-snippet")));
    WebSearchToolHandler handler = new WebSearchToolHandler(List.of(failed, fallback), "fallback");
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("web-search-fallback"));

    var result = handler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_WEB_SEARCH,
        Map.of("q", "hello", "provider", "failed"),
        context));

    Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
    Assertions.assertEquals("fallback", result.metadata().get("provider"));
    Assertions.assertEquals(Boolean.TRUE, result.metadata().get("fallbackUsed"));
  }

  @Test
  void shouldReturnErrorWhenProviderNotFound() throws Exception {
    SearchProvider fallback = new FixedProvider("fallback", List.of(new SearchResultItem("F", "https://f", "F-snippet")));
    WebSearchToolHandler handler = new WebSearchToolHandler(List.of(fallback), "fallback");
    ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("web-search-none"));

    var result = handler.handle(new ToolCallRequest(
        ConnectorToolPlugin.TOOL_WEB_SEARCH,
        Map.of("q", "hello", "provider", "unknown"),
        context));

    Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
    Assertions.assertEquals("SEARCH_PROVIDER_NOT_FOUND", result.errorCode());
  }

  private record FixedProvider(String id, List<SearchResultItem> items) implements SearchProvider {
    @Override
    public List<SearchResultItem> search(String query, int limit, Duration timeout) {
      return items.size() <= limit ? items : items.subList(0, limit);
    }
  }
}
