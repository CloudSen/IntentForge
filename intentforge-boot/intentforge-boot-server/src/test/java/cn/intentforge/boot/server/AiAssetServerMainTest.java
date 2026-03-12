package cn.intentforge.boot.server;

import cn.intentforge.api.agent.AgentRunCreateRequest;
import cn.intentforge.api.agent.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AiAssetServerMainTest {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void shouldStartWithoutDemoRuntimeState() throws Exception {
    Path workspace = Files.createTempDirectory("boot-server-main-workspace");
    Files.writeString(workspace.resolve("README.md"), "agent");
    Path pluginsDirectory = Files.createTempDirectory("boot-server-main-plugins");

    try (AiAssetServerRuntime runtime = AiAssetServerMain.startServer(new InetSocketAddress("127.0.0.1", 0), pluginsDirectory)) {
      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(
          HttpRequest.newBuilder(runtime.baseUri().resolve("/api/agent-runs"))
              .timeout(Duration.ofSeconds(10))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(new AgentRunCreateRequest(
                  "task-1",
                  "session-1",
                  null,
                  workspace.toString(),
                  "FULL",
                  "Implement event-driven agent runtime",
                  null,
                  Map.of("story", "IF-502")))))
              .build(),
          HttpResponse.BodyHandlers.ofString());

      Assertions.assertEquals(400, response.statusCode());
      ErrorResponse error = OBJECT_MAPPER.readValue(response.body(), ErrorResponse.class);
      Assertions.assertEquals("AGENT_RUN_REQUEST_INVALID", error.code());
      Assertions.assertTrue(error.message().contains("session not found"));
    }
  }
}
