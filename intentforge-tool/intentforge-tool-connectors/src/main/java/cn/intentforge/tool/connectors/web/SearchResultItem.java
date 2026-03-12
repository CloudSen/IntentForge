package cn.intentforge.tool.connectors.web;

/**
 * One search result entry.
 *
 * @param title result title
 * @param url result URL
 * @param snippet result snippet
 */
public record SearchResultItem(String title, String url, String snippet) {
  /**
   * Creates one search result item.
   *
   * @param title result title
   * @param url result URL
   * @param snippet result snippet
   */
  public SearchResultItem {
    title = normalize(title);
    url = normalize(url);
    snippet = normalize(snippet);
    if (title == null && snippet == null) {
      throw new IllegalArgumentException("title and snippet cannot both be blank");
    }
  }

  private static String normalize(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
