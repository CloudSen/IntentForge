package cn.intentforge.governance.agent;

import cn.intentforge.space.ResolvedSpaceProfile;

/**
 * Resolves the runtime components selected for one run after space configuration is known.
 */
public interface AgentRuntimeResolver {
  /**
   * Resolves runtime components and their observable selection metadata for the provided space profile.
   *
   * @param resolvedSpaceProfile effective space profile for the current run
   * @return resolved runtime components
   */
  ResolvedAgentRuntime resolve(ResolvedSpaceProfile resolvedSpaceProfile);
}
