package cn.intentforge.tool.connectors.web;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.util.OutputTruncator;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for {@code intentforge.web.fetch}.
 */
public final class WebFetchToolHandler implements ToolHandler {
  private static final int DEFAULT_TIMEOUT_MS = 10_000;
  private static final int DEFAULT_MAX_BYTES = 300_000;

  /**
   * Executes one web fetch request.
   *
   * @param request tool call request
   * @return tool call result
   */
  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    String url = readString(request.parameters(), "url");
    if (url == null) {
      return ToolCallResult.error("WEB_INVALID_ARGUMENT", "url is required");
    }
    String method = readString(request.parameters(), "method");
    if (method == null) {
      method = "GET";
    }
    method = method.toUpperCase(Locale.ROOT);
    String body = readString(request.parameters(), "body");
    int timeoutMs = readInt(request.parameters(), "timeoutMs", DEFAULT_TIMEOUT_MS);
    int maxBytes = readInt(request.parameters(), "maxBytes", DEFAULT_MAX_BYTES);
    boolean followRedirects = readBoolean(request.parameters(), "followRedirects", true);

    try {
      HttpClient client = HttpClient.newBuilder()
          .followRedirects(followRedirects ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER)
          .connectTimeout(Duration.ofMillis(timeoutMs))
          .build();
      HttpRequest.Builder builder = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .timeout(Duration.ofMillis(timeoutMs));
      Map<String, String> headers = readHeaders(request.parameters().get("headers"));
      headers.forEach(builder::header);

      if ("GET".equals(method) || "DELETE".equals(method) || "HEAD".equals(method)) {
        builder.method(method, HttpRequest.BodyPublishers.noBody());
      } else {
        builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
      }

      HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
      byte[] responseBytes = response.body() == null ? new byte[0] : response.body();
      boolean truncated = responseBytes.length > maxBytes;
      String responseBody = new String(
          truncated ? java.util.Arrays.copyOf(responseBytes, Math.max(0, maxBytes)) : responseBytes,
          StandardCharsets.UTF_8);

      Map<String, Object> metadata = new LinkedHashMap<>();
      metadata.put("statusCode", response.statusCode());
      metadata.put("responseBytes", responseBytes.length);
      metadata.put("truncated", truncated);
      metadata.put("url", response.uri().toString());
      metadata.put("followRedirects", followRedirects);

      if (truncated) {
        Path outputPath = request.context().toolOutputDirectory()
            .resolve("web-fetch-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + ".bin");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, responseBytes);
        metadata.put("outputPath", outputPath.toString());
      }

      OutputTruncator.TruncateResult truncateResult =
          OutputTruncator.truncate(responseBody, request.context(), "web-fetch-body");
      metadata.put("inlineTruncated", truncateResult.truncated());
      if (truncateResult.outputPath() != null) {
        metadata.put("inlineOutputPath", truncateResult.outputPath().toString());
      }

      Map<String, Object> structured = new LinkedHashMap<>();
      structured.put("statusCode", response.statusCode());
      structured.put("url", response.uri().toString());
      structured.put("headers", response.headers().map());
      structured.put("body", truncateResult.content());
      structured.put("truncated", truncated || truncateResult.truncated());

      return ToolCallResult.success(truncateResult.content(), Map.copyOf(structured), Map.copyOf(metadata));
    } catch (HttpTimeoutException ex) {
      return ToolCallResult.error("WEB_TIMEOUT", ex.getMessage());
    } catch (IllegalArgumentException ex) {
      return ToolCallResult.error("WEB_INVALID_ARGUMENT", ex.getMessage());
    } catch (IOException ex) {
      return ToolCallResult.error("WEB_IO_ERROR", ex.getMessage());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return ToolCallResult.error("WEB_INTERRUPTED", ex.getMessage());
    } catch (Exception ex) {
      return ToolCallResult.error("WEB_FETCH_ERROR", ex.getMessage());
    }
  }

  private static Map<String, String> readHeaders(Object value) {
    if (!(value instanceof Map<?, ?> headerMap) || headerMap.isEmpty()) {
      return Map.of();
    }
    Map<String, String> normalized = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : headerMap.entrySet()) {
      String key = normalize(entry.getKey());
      if (key == null) {
        continue;
      }
      String headerValue = normalize(entry.getValue());
      if (headerValue == null) {
        continue;
      }
      normalized.put(key, headerValue);
    }
    return Map.copyOf(normalized);
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
      return Math.max(1, number.intValue());
    }
    return Math.max(1, Integer.parseInt(String.valueOf(value)));
  }

  private static boolean readBoolean(Map<String, Object> parameters, String key, boolean defaultValue) {
    Object value = parameters.get(key);
    if (value == null) {
      return defaultValue;
    }
    if (value instanceof Boolean bool) {
      return bool;
    }
    return Boolean.parseBoolean(String.valueOf(value));
  }

  private static String normalize(Object value) {
    if (value == null) {
      return null;
    }
    String normalized = String.valueOf(value).trim();
    return normalized.isEmpty() ? null : normalized;
  }
}
