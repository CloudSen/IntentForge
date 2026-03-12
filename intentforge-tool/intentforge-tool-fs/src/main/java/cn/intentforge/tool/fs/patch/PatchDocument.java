package cn.intentforge.tool.fs.patch;

import java.util.List;

/**
 * Parsed patch document.
 *
 * @param operations patch operations
 */
public record PatchDocument(List<PatchOperation> operations) {
}
