package cn.intentforge.tool.fs.patch;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for Codex/OpenCode style patch text.
 */
public final class PatchParser {
  private static final String BEGIN = "*** Begin Patch";
  private static final String END = "*** End Patch";

  /**
   * Parses patch text with Codex/OpenCode grammar.
   *
   * @param patchText patch text
   * @return parsed patch document
   */
  public PatchDocument parse(String patchText) {
    String normalizedPatchText = normalizeLineEndings(patchText);
    List<String> lines = normalizedLines(normalizedPatchText);
    if (lines.isEmpty() || !BEGIN.equals(lines.get(0)) || !END.equals(lines.get(lines.size() - 1))) {
      throw new IllegalArgumentException("Patch must start with '*** Begin Patch' and end with '*** End Patch'");
    }

    List<PatchOperation> operations = new ArrayList<>();
    int index = 1;
    while (index < lines.size() - 1) {
      String header = lines.get(index);
      if (header.startsWith("*** Add File: ")) {
        String path = header.substring("*** Add File: ".length()).trim();
        if (path.isEmpty()) {
          throw new IllegalArgumentException("Add File path must not be blank");
        }
        index++;
        List<String> body = new ArrayList<>();
        while (index < lines.size() - 1 && !lines.get(index).startsWith("*** ")) {
          String line = lines.get(index);
          if (!line.startsWith("+")) {
            throw new IllegalArgumentException("Add File body lines must start with '+'");
          }
          body.add(line.substring(1));
          index++;
        }
        operations.add(new PatchOperation(PatchOperationType.ADD, path, null, List.copyOf(body)));
        continue;
      }

      if (header.startsWith("*** Delete File: ")) {
        String path = header.substring("*** Delete File: ".length()).trim();
        if (path.isEmpty()) {
          throw new IllegalArgumentException("Delete File path must not be blank");
        }
        operations.add(new PatchOperation(PatchOperationType.DELETE, path, null, List.of()));
        index++;
        continue;
      }

      if (header.startsWith("*** Update File: ")) {
        String path = header.substring("*** Update File: ".length()).trim();
        if (path.isEmpty()) {
          throw new IllegalArgumentException("Update File path must not be blank");
        }
        index++;

        String moveTo = null;
        if (index < lines.size() - 1 && lines.get(index).startsWith("*** Move to: ")) {
          moveTo = lines.get(index).substring("*** Move to: ".length()).trim();
          if (moveTo.isEmpty()) {
            throw new IllegalArgumentException("Move to path must not be blank");
          }
          index++;
        }

        List<String> body = new ArrayList<>();
        while (index < lines.size() - 1) {
          String line = lines.get(index);
          if ("*** End of File".equals(line)) {
            index++;
            continue;
          }
          if (line.startsWith("*** ")) {
            break;
          }
          if (!line.startsWith("@@")
              && !line.startsWith("+")
              && !line.startsWith("-")
              && !line.startsWith(" ")) {
            throw new IllegalArgumentException("Invalid update body line: " + line);
          }
          body.add(line);
          index++;
        }

        operations.add(new PatchOperation(PatchOperationType.UPDATE, path, moveTo, List.copyOf(body)));
        continue;
      }

      throw new IllegalArgumentException("Unsupported patch header: " + header);
    }

    if (operations.isEmpty()) {
      throw new IllegalArgumentException("Patch does not contain operations");
    }

    return new PatchDocument(List.copyOf(operations));
  }

  private static String normalizeLineEndings(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("\r\n", "\n").replace('\r', '\n');
  }

  private static List<String> normalizedLines(String text) {
    String[] split = text.split("\n", -1);
    List<String> lines = new ArrayList<>(List.of(split));
    while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
      lines.remove(lines.size() - 1);
    }
    return lines;
  }
}
