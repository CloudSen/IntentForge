package cn.intentforge.config;

import java.util.List;
import java.util.Optional;

/**
 * Persistent store contract for user-managed space configuration snapshots.
 */
public interface SpaceConfigurationStore {
  /**
   * Loads one space configuration snapshot by space identifier.
   *
   * @param spaceId target space identifier
   * @return stored configuration when present
   */
  Optional<SpaceConfiguration> find(String spaceId);

  /**
   * Lists all stored space configuration snapshots.
   *
   * @return stored configurations
   */
  List<SpaceConfiguration> list();

  /**
   * Saves or replaces one space configuration snapshot.
   *
   * @param configuration configuration snapshot to persist
   */
  void save(SpaceConfiguration configuration);

  /**
   * Deletes one space configuration snapshot.
   *
   * @param spaceId target space identifier
   */
  void delete(String spaceId);
}
