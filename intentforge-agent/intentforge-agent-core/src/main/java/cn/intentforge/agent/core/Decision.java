package cn.intentforge.agent.core;

import java.util.Map;
import java.util.Objects;

/**
 * Outcome summary produced by one routed agent stage.
 *
 * @param agentId emitting agent identifier
 * @param role emitting agent role
 * @param summary short stage outcome summary
 * @param metadata stage metadata
 */
public record Decision(
    String agentId,
    AgentRole role,
    String summary,
    Map<String, String> metadata
) {
  /**
   * Creates a validated decision.
   */
  public Decision {
    agentId = AgentModelSupport.requireText(agentId, "agentId");
    role = Objects.requireNonNull(role, "role must not be null");
    summary = AgentModelSupport.requireText(summary, "summary");
    metadata = AgentModelSupport.immutableMetadata(metadata);
  }
}
