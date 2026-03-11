package cn.intentforge.common.plugin;

import java.nio.file.Path;
import java.util.List;

public final class PluginHandle<T> implements AutoCloseable {
  private final PluginMetadata metadata;
  private final Path jarPath;
  private final List<T> services;
  private final AutoCloseable closeable;

  private PluginState state;
  private String message;

  public PluginHandle(
      PluginMetadata metadata,
      Path jarPath,
      List<T> services,
      AutoCloseable closeable,
      PluginState state,
      String message
  ) {
    this.metadata = metadata;
    this.jarPath = jarPath;
    this.services = List.copyOf(services);
    this.closeable = closeable;
    this.state = state;
    this.message = message;
  }

  public PluginMetadata metadata() {
    return metadata;
  }

  public Path jarPath() {
    return jarPath;
  }

  public List<T> services() {
    return services;
  }

  public PluginState state() {
    return state;
  }

  public String message() {
    return message;
  }

  public boolean active() {
    return state == PluginState.ACTIVE;
  }

  public void markStopped(String reason) {
    state = PluginState.STOPPED;
    message = reason;
  }

  @Override
  public void close() {
    if (closeable == null) {
      return;
    }
    try {
      closeable.close();
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to close plugin classloader for " + metadata.id(), ex);
    }
  }
}
