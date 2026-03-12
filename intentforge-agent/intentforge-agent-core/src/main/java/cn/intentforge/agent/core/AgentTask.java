package cn.intentforge.agent.core;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable request used to execute one routed coding task.
 *
 * @param id stable task identifier
 * @param sessionId target session identifier
 * @param spaceId optional explicit space identifier; when absent the session space is used
 * @param workspaceRoot workspace root used for tool execution
 * @param mode requested execution depth
 * @param intent user intent or objective
 * @param targetAgentId optional explicit target agent identifier
 * @param metadata task metadata
 */
public record AgentTask(
    String id,
    String sessionId,
    String spaceId,
    Path workspaceRoot,
    TaskMode mode,
    String intent,
    String targetAgentId,
    Map<String, String> metadata
) {
  /**
   * Creates a validated task request.
   */
  public AgentTask {
    id = AgentModelSupport.requireText(id, "id");
    sessionId = AgentModelSupport.requireText(sessionId, "sessionId");
    spaceId = AgentModelSupport.normalize(spaceId);
    workspaceRoot = AgentModelSupport.normalizeWorkspace(workspaceRoot);
    mode = Objects.requireNonNullElse(mode, TaskMode.FULL);
    intent = AgentModelSupport.requireText(intent, "intent");
    targetAgentId = AgentModelSupport.normalize(targetAgentId);
    metadata = AgentModelSupport.immutableMetadata(metadata);
  }
}
