package cn.intentforge.tool.fs.patch;

import java.util.List;

/**
 * One parsed patch operation.
 *
 * @param type operation type
 * @param path target path
 * @param moveTo optional move target path
 * @param bodyLines operation body lines
 */
public record PatchOperation(
    PatchOperationType type,
    String path,
    String moveTo,
    List<String> bodyLines
) {
}
