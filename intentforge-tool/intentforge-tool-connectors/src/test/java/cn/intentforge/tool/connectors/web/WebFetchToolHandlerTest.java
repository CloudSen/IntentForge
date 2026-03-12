package cn.intentforge.tool.connectors.web;

import cn.intentforge.tool.connectors.ConnectorToolPlugin;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallStatus;
import cn.intentforge.tool.core.model.ToolExecutionContext;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebFetchToolHandlerTest {
  @Test
  void shouldFollowRedirect() throws Exception {
    try (TestServer server = TestServer.start()) {
      WebFetchToolHandler handler = new WebFetchToolHandler();
      ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("web-fetch-redirect"));

      var result = handler.handle(new ToolCallRequest(
          ConnectorToolPlugin.TOOL_WEB_FETCH,
          Map.of("url", server.uri("/redirect").toString(), "followRedirects", true),
          context));
      Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
      @SuppressWarnings("unchecked")
      Map<String, Object> structured = (Map<String, Object>) result.structured();
      Assertions.assertEquals(200, structured.get("statusCode"));
      Assertions.assertTrue(String.valueOf(structured.get("body")).contains("OK"));
    }
  }

  @Test
  void shouldTimeout() throws Exception {
    try (TestServer server = TestServer.start()) {
      WebFetchToolHandler handler = new WebFetchToolHandler();
      ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("web-fetch-timeout"));

      var result = handler.handle(new ToolCallRequest(
          ConnectorToolPlugin.TOOL_WEB_FETCH,
          Map.of("url", server.uri("/slow").toString(), "timeoutMs", 50),
          context));
      Assertions.assertEquals(ToolCallStatus.ERROR, result.status());
      Assertions.assertEquals("WEB_TIMEOUT", result.errorCode());
    }
  }

  @Test
  void shouldTruncateLargeResponse() throws Exception {
    try (TestServer server = TestServer.start()) {
      WebFetchToolHandler handler = new WebFetchToolHandler();
      ToolExecutionContext context = ToolExecutionContext.create(Files.createTempDirectory("web-fetch-large"));

      var result = handler.handle(new ToolCallRequest(
          ConnectorToolPlugin.TOOL_WEB_FETCH,
          Map.of("url", server.uri("/large").toString(), "maxBytes", 32),
          context));
      Assertions.assertEquals(ToolCallStatus.SUCCESS, result.status());
      Assertions.assertTrue((Boolean) result.metadata().get("truncated"));
      Assertions.assertNotNull(result.metadata().get("outputPath"));
    }
  }

  private static final class TestServer implements AutoCloseable {
    private final HttpServer server;

    private TestServer(HttpServer server) {
      this.server = server;
    }

    static TestServer start() throws Exception {
      HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
      server.createContext("/ok", exchange -> {
        byte[] body = "OK".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
      });
      server.createContext("/redirect", exchange -> {
        exchange.getResponseHeaders().set("Location", "/ok");
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
      });
      server.createContext("/slow", exchange -> {
        try {
          Thread.sleep(Duration.ofMillis(300));
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }
        byte[] body = "slow".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
      });
      server.createContext("/large", exchange -> {
        byte[] body = "x".repeat(5_000).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
      });
      server.start();
      return new TestServer(server);
    }

    URI uri(String path) {
      return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    @Override
    public void close() {
      server.stop(0);
    }
  }
}
