package cn.intentforge.tool.connectors.web;

import java.time.Duration;
import java.util.List;

/**
 * SPI for search provider implementations.
 */
public interface SearchProvider {
  /**
   * Returns unique provider identifier.
   *
   * @return provider identifier
   */
  String id();

  /**
   * Executes search query.
   *
   * @param query query text
   * @param limit max result count
   * @param timeout request timeout
   * @return search results
   */
  List<SearchResultItem> search(String query, int limit, Duration timeout);
}
