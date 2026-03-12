package cn.intentforge.tool.fs;

import cn.intentforge.tool.core.ToolHandler;
import cn.intentforge.tool.core.model.ToolCallRequest;
import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.fs.patch.PatchApplier;
import cn.intentforge.tool.fs.patch.PatchDocument;
import cn.intentforge.tool.fs.patch.PatchParser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for {@code intentforge.fs.apply-patch}.
 */
final class FsApplyPatchToolHandler implements ToolHandler {
  private final PatchParser parser = new PatchParser();
  private final PatchApplier applier = new PatchApplier();

  @Override
  public ToolCallResult handle(ToolCallRequest request) {
    try {
      String patch = FsToolSupport.readString(request.parameters(), "patch");
      if (patch == null) {
        return ToolCallResult.error("FS_INVALID_ARGUMENT", "patch is required");
      }

      PatchDocument document = parser.parse(patch);
      List<String> changedFiles = applier.apply(request.context(), document);

      Map<String, Object> structured = new LinkedHashMap<>();
      structured.put("changedFiles", List.copyOf(changedFiles));
      structured.put("count", changedFiles.size());

      return ToolCallResult.success(
          "Applied patch successfully. Changed files: " + String.join(", ", changedFiles),
          Map.copyOf(structured),
          Map.of());
    } catch (Exception ex) {
      return ToolCallResult.error("FS_APPLY_PATCH_ERROR", ex.getMessage());
    }
  }
}
