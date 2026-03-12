package cn.intentforge.tool.connectors.web;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.util.OutputTruncator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Handler for {@code intentforge.web.search}.
 */
public final class WebSearchToolHandler implements ToolHandler {
  private static final int DEFAULT_LIMIT = 5;
  private static final int MAX_LIMIT = 20;
  private static final int DEFAULT_TIMEOUT_MS = 10_000;

  private final Map<String, SearchProvider> providersById;
  private final String fallbackProviderId;

  /**
   * Creates handler with SPI-loaded providers and built-in fallback provider.
   */
  public WebSearchToolHandler() {
    this(loadProviders(Thread.currentThread().getContextClassLoader()), BuiltinDuckDuckGoSearchProvider.PROVIDER_ID);
  }

  /**
   * Creates handler.
   *
   * @param providers available search providers
   * @param fallbackProviderId fallback provider identifier
   */
  public WebSearchToolHandler(Collection<SearchProvider> providers, String fallbackProviderId) {
    Map<String, SearchProvider> providerMap = new LinkedHashMap<>();
    if (providers != null) {
      for (SearchProvider provider : providers) {
        if (provider == null) {
          continue;
        }
        String providerId = normalize(provider.id());
        if (providerId == null) {
          continue;
        }
        providerMap.put(providerId, provider);
      }
    }
    if (providerMap.isEmpty()) {
      throw new IllegalArgumentException("providers must not be empty");
    }
    this.providersById = Map.copyOf(providerMap);

    String normalizedFallback = normalize(fallbackProviderId);
    if (normalizedFallback == null || !providerMap.containsKey(normalizedFallback)) {
      normalizedFallback = providerMap.keySet().iterator().next();
    }
    this.fallbackProviderId = normalizedFallback;
  }

  /**
   * Executes one search call.
   *
   * @param request tool call request
   * @return tool call result
   */
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    String query = readString(request.parameters(), "q");
    if (query == null) {
      return ToolCallResult.error("SEARCH_INVALID_ARGUMENT", "q is required");
    }
    String requestedProviderId = readString(request.parameters(), "provider");
    int limit = Math.min(MAX_LIMIT, readInt(request.parameters(), "limit", DEFAULT_LIMIT));
    if (limit < 1) {
      limit = DEFAULT_LIMIT;
    }
    Duration timeout = Duration.ofMillis(Math.max(1, readInt(request.parameters(), "timeoutMs", DEFAULT_TIMEOUT_MS)));

    SearchProvider fallbackProvider = providersById.get(fallbackProviderId);
    SearchProvider selectedProvider;
    if (requestedProviderId == null) {
      selectedProvider = fallbackProvider;
    } else {
      selectedProvider = providersById.get(requestedProviderId.toLowerCase(Locale.ROOT));
      if (selectedProvider == null) {
        return ToolCallResult.error("SEARCH_PROVIDER_NOT_FOUND", "Search provider not found: " + requestedProviderId);
      }
    }

    List<SearchResultItem> results;
    String usedProviderId = selectedProvider.id();
    boolean fallbackUsed = false;
    try {
      results = selectedProvider.search(query, limit, timeout);
    } catch (Exception ex) {
      if (selectedProvider != fallbackProvider && fallbackProvider != null) {
        try {
          results = fallbackProvider.search(query, limit, timeout);
          usedProviderId = fallbackProvider.id();
          fallbackUsed = true;
        } catch (Exception fallbackException) {
          return ToolCallResult.error(
              "SEARCH_FAILED",
              "Provider " + selectedProvider.id() + " failed: " + ex.getMessage()
                  + "; fallback failed: " + fallbackException.getMessage());
        }
      } else {
        return ToolCallResult.error("SEARCH_FAILED", ex.getMessage());
      }
    }

    List<Map<String, Object>> structuredResults = new ArrayList<>();
    StringBuilder outputBuilder = new StringBuilder();
    int index = 1;
    for (SearchResultItem result : results == null ? List.<SearchResultItem>of() : results) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("title", result.title());
      item.put("url", result.url());
      item.put("snippet", result.snippet());
      structuredResults.add(Map.copyOf(item));

      outputBuilder.append(index++).append(". ").append(Objects.toString(result.title(), "(no title)"));
      if (result.url() != null) {
        outputBuilder.append('\n').append("   ").append(result.url());
      }
      if (result.snippet() != null) {
        outputBuilder.append('\n').append("   ").append(result.snippet());
      }
      outputBuilder.append('\n');
    }
    if (structuredResults.isEmpty()) {
      outputBuilder.append("No search results");
    }

    OutputTruncator.TruncateResult truncation =
        OutputTruncator.truncate(outputBuilder.toString(), request.context(), "web-search");
    Map<String, Object> structured = new LinkedHashMap<>();
    structured.put("query", query);
    structured.put("provider", usedProviderId);
    structured.put("results", List.copyOf(structuredResults));

    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("requestedProvider", requestedProviderId == null ? fallbackProviderId : requestedProviderId);
    metadata.put("provider", usedProviderId);
    metadata.put("fallbackUsed", fallbackUsed);
    metadata.put("count", structuredResults.size());
    metadata.put("truncated", truncation.truncated());
    if (truncation.outputPath() != null) {
      metadata.put("outputPath", truncation.outputPath().toString());
    }

    return ToolCallResult.success(truncation.content(), Map.copyOf(structured), Map.copyOf(metadata));
  }

  private static List<SearchProvider> loadProviders(ClassLoader classLoader) {
    List<SearchProvider> providers = new ArrayList<>();
    providers.add(new BuiltinDuckDuckGoSearchProvider());
    ServiceLoader<SearchProvider> loader = ServiceLoader.load(SearchProvider.class, classLoader);
    for (ServiceLoader.Provider<SearchProvider> provider : loader.stream().toList()) {
      SearchProvider instance = provider.get();
      if (instance == null) {
        continue;
      }
      providers.add(instance);
    }
    return List.copyOf(providers);
  }

  private static String readString(Map<String, Object> parameters, String key) {
    return normalize(parameters.get(key));
  }

  private static int readInt(Map<String, Object> parameters, String key, int defaultValue) {
    Object value = parameters.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Number number) {
      return number.intValue();
    }
    return Integer.parseInt(String.valueOf(value));
  }

  private static String normalize(Object value) {
    if (value == null) {
      return null;
    }
    String normalized = String.valueOf(value).trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
