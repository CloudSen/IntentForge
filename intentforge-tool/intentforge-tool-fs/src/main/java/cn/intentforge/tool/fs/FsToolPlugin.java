package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.model.ToolDefinition;
import cn.intentforge.tool.core.registry.ToolRegistration;
import cn.intentforge.tool.core.spi.ToolPlugin;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Provides filesystem tools for coding workflows.
 */
public final class FsToolPlugin implements ToolPlugin {
  /**
   * Tool id: read file content.
   */
  public static final String TOOL_READ = "intentforge.fs.read";
  /**
   * Tool id: list directory.
   */
  public static final String TOOL_LIST = "intentforge.fs.list";
  /**
   * Tool id: glob search.
   */
  public static final String TOOL_GLOB = "intentforge.fs.glob";
  /**
   * Tool id: grep search.
   */
  public static final String TOOL_GREP = "intentforge.fs.grep";
  /**
   * Tool id: write file.
   */
  public static final String TOOL_WRITE = "intentforge.fs.write";
  /**
   * Tool id: string replace edit.
   */
  public static final String TOOL_EDIT = "intentforge.fs.edit";
  /**
   * Tool id: apply patch.
   */
  public static final String TOOL_APPLY_PATCH = "intentforge.fs.apply-patch";

  @Override
  public Collection<ToolRegistration> tools() {
    return List.of(
        new ToolRegistration(new ToolDefinition(
            TOOL_READ,
            "Read text file content with optional line range.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "path", Map.of("type", "string"),
                    "startLine", Map.of("type", "integer", "minimum", 1),
                    "endLine", Map.of("type", "integer", "minimum", 1)),
                "required", List.of("path")),
            false), new FsReadToolHandler()),
        new ToolRegistration(new ToolDefinition(
            TOOL_LIST,
            "List files under directory.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "path", Map.of("type", "string"))),
            false), new FsListToolHandler()),
        new ToolRegistration(new ToolDefinition(
            TOOL_GLOB,
            "Search files by glob pattern.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "path", Map.of("type", "string"),
                    "pattern", Map.of("type", "string")),
                "required", List.of("pattern")),
            false), new FsGlobToolHandler()),
        new ToolRegistration(new ToolDefinition(
            TOOL_GREP,
            "Search file content by regex.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "path", Map.of("type", "string"),
                    "pattern", Map.of("type", "string"),
                    "include", Map.of("type", "string")),
                "required", List.of("pattern")),
            false), new FsGrepToolHandler()),
        new ToolRegistration(new ToolDefinition(
            TOOL_WRITE,
            "Write text file content.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "path", Map.of("type", "string"),
                    "content", Map.of("type", "string")),
                "required", List.of("path", "content")),
            true), new FsWriteToolHandler()),
        new ToolRegistration(new ToolDefinition(
            TOOL_EDIT,
            "Edit file content by replacing text.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "path", Map.of("type", "string"),
                    "oldString", Map.of("type", "string"),
                    "newString", Map.of("type", "string"),
                    "replaceAll", Map.of("type", "boolean")),
                "required", List.of("path", "oldString", "newString")),
            true), new FsEditToolHandler()),
        new ToolRegistration(new ToolDefinition(
            TOOL_APPLY_PATCH,
            "Apply patch in Codex/OpenCode format.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "patch", Map.of("type", "string")),
                "required", List.of("patch")),
            true), new FsApplyPatchToolHandler()));
  }
}
