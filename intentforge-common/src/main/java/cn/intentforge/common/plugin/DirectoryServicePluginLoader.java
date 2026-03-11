package cn.intentforge.common.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class DirectoryServicePluginLoader<T> {
  public static final String DEFAULT_METADATA_LOCATION = "META-INF/intentforge-plugin.properties";

  private final Path pluginsDirectory;
  private final Class<T> serviceType;
  private final String expectedApiVersion;
  private final String runtimeVersion;
  private final ClassLoader parentClassLoader;
  private final String metadataLocation;

  private final Map<String, Path> pluginJarsById = new LinkedHashMap<>();
  private final Map<String, PluginHandle<T>> handlesById = new LinkedHashMap<>();

  public DirectoryServicePluginLoader(
      Path pluginsDirectory,
      Class<T> serviceType,
      String expectedApiVersion,
      String runtimeVersion
  ) {
    this(pluginsDirectory, serviceType, expectedApiVersion, runtimeVersion,
        Thread.currentThread().getContextClassLoader(), DEFAULT_METADATA_LOCATION);
  }

  public DirectoryServicePluginLoader(
      Path pluginsDirectory,
      Class<T> serviceType,
      String expectedApiVersion,
      String runtimeVersion,
      ClassLoader parentClassLoader,
      String metadataLocation
  ) {
    this.pluginsDirectory = pluginsDirectory;
    this.serviceType = serviceType;
    this.expectedApiVersion = expectedApiVersion;
    this.runtimeVersion = runtimeVersion;
    this.parentClassLoader = parentClassLoader == null
        ? DirectoryServicePluginLoader.class.getClassLoader()
        : parentClassLoader;
    this.metadataLocation = metadataLocation == null ? DEFAULT_METADATA_LOCATION : metadataLocation;
  }

  public synchronized List<PluginHandle<T>> loadAll() {
    Map<String, Path> discovered = discoverPluginJars();
    stopRemovedPlugins(discovered.keySet());
    pluginJarsById.clear();
    pluginJarsById.putAll(discovered);

    List<PluginHandle<T>> handles = new ArrayList<>();
    for (Map.Entry<String, Path> entry : discovered.entrySet()) {
      PluginHandle<T> previous = handlesById.remove(entry.getKey());
      if (previous != null && previous.active()) {
        previous.close();
      }
      PluginHandle<T> handle = loadFromJar(entry.getKey(), entry.getValue());
      handlesById.put(entry.getKey(), handle);
      handles.add(handle);
    }
    return List.copyOf(handles);
  }

  public synchronized Optional<PluginHandle<T>> start(String pluginId) {
    Path jarPath = pluginJarsById.get(pluginId);
    if (jarPath == null) {
      pluginJarsById.putAll(discoverPluginJars());
      jarPath = pluginJarsById.get(pluginId);
    }
    if (jarPath == null) {
      return Optional.empty();
    }
    PluginHandle<T> previous = handlesById.remove(pluginId);
    if (previous != null && previous.active()) {
      previous.close();
    }
    PluginHandle<T> handle = loadFromJar(pluginId, jarPath);
    handlesById.put(pluginId, handle);
    return Optional.of(handle);
  }

  public synchronized Optional<PluginHandle<T>> stop(String pluginId) {
    PluginHandle<T> handle = handlesById.get(pluginId);
    if (handle == null) {
      return Optional.empty();
    }
    if (handle.active()) {
      handle.close();
    }
    handle.markStopped("Stopped by runtime");
    return Optional.of(handle);
  }

  public synchronized List<PluginHandle<T>> list() {
    return List.copyOf(handlesById.values());
  }

  private Map<String, Path> discoverPluginJars() {
    try {
      Files.createDirectories(pluginsDirectory);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to create plugin directory " + pluginsDirectory, ex);
    }

    Map<String, Path> discovered = new LinkedHashMap<>();
    try (var stream = Files.list(pluginsDirectory)) {
      stream
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".jar"))
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .forEach(path -> {
            PluginMetadata metadata = readMetadata(path);
            discovered.put(metadata.id(), path);
          });
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to scan plugin directory " + pluginsDirectory, ex);
    }
    return discovered;
  }

  private void stopRemovedPlugins(Collection<String> discoveredIds) {
    List<String> removed = new ArrayList<>();
    for (String pluginId : handlesById.keySet()) {
      if (!discoveredIds.contains(pluginId)) {
        removed.add(pluginId);
      }
    }
    for (String pluginId : removed) {
      PluginHandle<T> removedHandle = handlesById.remove(pluginId);
      if (removedHandle == null) {
        continue;
      }
      if (removedHandle.active()) {
        removedHandle.close();
      }
      removedHandle.markStopped("Plugin jar removed");
    }
  }

  private PluginHandle<T> loadFromJar(String pluginId, Path jarPath) {
    PluginMetadata metadata = readMetadata(jarPath);
    if (!metadata.enabled()) {
      return new PluginHandle<>(metadata, jarPath, List.of(), null, PluginState.DISABLED, "Plugin disabled");
    }

    Optional<String> compatibilityError =
        PluginCompatibilityValidator.validate(metadata, expectedApiVersion, runtimeVersion);
    if (compatibilityError.isPresent()) {
      return new PluginHandle<>(metadata, jarPath, List.of(), null, PluginState.REJECTED, compatibilityError.get());
    }

    try {
      URLClassLoader classLoader = new URLClassLoader(new URL[]{jarPath.toUri().toURL()}, parentClassLoader);
      List<T> services = loadServices(classLoader);
      if (services.isEmpty()) {
        classLoader.close();
        return new PluginHandle<>(metadata, jarPath, List.of(), null, PluginState.REJECTED,
            "No service implementation found for " + serviceType.getName());
      }
      return new PluginHandle<>(metadata, jarPath, services, classLoader, PluginState.ACTIVE, "Plugin active");
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to load plugin " + pluginId + " from " + jarPath, ex);
    }
  }

  private List<T> loadServices(ClassLoader classLoader) {
    return ServiceLoader.load(serviceType, classLoader)
        .stream()
        .map(ServiceLoader.Provider::get)
        .toList();
  }

  private PluginMetadata readMetadata(Path jarPath) {
    String fallbackId = stripJarExtension(jarPath.getFileName().toString());
    try (JarFile jarFile = new JarFile(jarPath.toFile())) {
      JarEntry metadataEntry = jarFile.getJarEntry(metadataLocation);
      if (metadataEntry == null) {
        return new PluginMetadata(fallbackId, fallbackId, "unspecified", "Missing plugin metadata",
            false, null, null);
      }

      Properties properties = new Properties();
      try (InputStream inputStream = jarFile.getInputStream(metadataEntry)) {
        properties.load(inputStream);
      }
      overlaySidecar(jarPath, properties);
      return PluginMetadata.from(properties, fallbackId);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read plugin metadata from " + jarPath, ex);
    }
  }

  private void overlaySidecar(Path jarPath, Properties properties) throws IOException {
    Path sidecar = pluginsDirectory.resolve(stripJarExtension(jarPath.getFileName().toString()) + ".properties");
    if (!Files.exists(sidecar)) {
      return;
    }
    try (InputStream inputStream = Files.newInputStream(sidecar)) {
      Properties overrides = new Properties();
      overrides.load(inputStream);
      for (String name : overrides.stringPropertyNames()) {
        properties.setProperty(name, overrides.getProperty(name));
      }
    }
  }

  private static String stripJarExtension(String fileName) {
    int index = fileName.lastIndexOf(".jar");
    return index < 0 ? fileName : fileName.substring(0, index);
  }
}
