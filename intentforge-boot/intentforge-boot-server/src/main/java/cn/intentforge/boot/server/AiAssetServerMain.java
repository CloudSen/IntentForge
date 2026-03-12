package cn.intentforge.boot.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

/**
 * Minimal terminal entrypoint that starts the HTTP/SSE server for local execution.
 */
public final class AiAssetServerMain {
  private AiAssetServerMain() {
  }

  /**
   * Starts the minimal server and blocks the main thread until the process is terminated.
   *
   * <p>Supported system properties:
   * <ul>
   *   <li>{@code intentforge.server.host}</li>
   *   <li>{@code intentforge.server.port}</li>
   *   <li>{@code intentforge.pluginsDir}</li>
   * </ul>
   *
   * @param args unused CLI arguments
   * @throws Exception when startup fails
   */
  public static void main(String[] args) throws Exception {
    String host = System.getProperty("intentforge.server.host", AiAssetServerBootstrap.DEFAULT_HOST);
    int port = Integer.getInteger("intentforge.server.port", AiAssetServerBootstrap.DEFAULT_PORT);
    Path pluginsDirectory = Path.of(System.getProperty("intentforge.pluginsDir", "plugins"));

    AiAssetServerRuntime runtime = startServer(new InetSocketAddress(host, port), pluginsDirectory);

    Runtime.getRuntime().addShutdownHook(new Thread(runtime::close));

    System.out.println("IntentForge server started at: " + runtime.baseUri());
    System.out.println("create run endpoint: " + runtime.baseUri().resolve("/api/agent-runs"));
    System.out.println("request handling prefers virtual threads: true");
    System.out.println("runtime seed data is not preloaded");

    new CountDownLatch(1).await();
  }

  static AiAssetServerRuntime startServer(InetSocketAddress bindAddress, Path pluginsDirectory) throws IOException {
    return AiAssetServerBootstrap.bootstrap(bindAddress, pluginsDirectory, null, null);
  }
}
