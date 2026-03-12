package cn.intentforge.agent.core;

import java.util.Map;

/**
 * Immutable artifact emitted by one agent stage.
 *
 * @param name artifact file-like name
 * @param mediaType artifact media type
 * @param content artifact textual content
 * @param metadata artifact metadata
 */
public record Artifact(
    String name,
    String mediaType,
    String content,
    Map<String, String> metadata
) {
  /**
   * Creates a validated artifact.
   */
  public Artifact {
    name = AgentModelSupport.requireText(name, "name");
    mediaType = AgentModelSupport.requireText(mediaType, "mediaType");
    content = AgentModelSupport.requireText(content, "content");
    metadata = AgentModelSupport.immutableMetadata(metadata);
  }
}
