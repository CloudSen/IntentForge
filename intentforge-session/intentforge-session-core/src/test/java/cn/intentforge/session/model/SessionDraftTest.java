package cn.intentforge.session.model;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SessionDraftTest {
  @Test
  void shouldNormalizeDraftFieldsAndMetadata() {
    SessionDraft draft = new SessionDraft(
        " session-1 ",
        " Planning Session ",
        " application-alpha ",
        Map.of(" topic ", " spi "));

    Assertions.assertEquals("session-1", draft.id());
    Assertions.assertEquals("Planning Session", draft.title());
    Assertions.assertEquals("application-alpha", draft.spaceId());
    Assertions.assertEquals(Map.of("topic", "spi"), draft.metadata());
    Assertions.assertThrows(UnsupportedOperationException.class, () -> draft.metadata().put("extra", "value"));
  }

  @Test
  void shouldRejectBlankRequiredFieldsAndInvalidMetadata() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new SessionDraft(" ", null, null, Map.of()));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SessionDraft("session-1", null, null, Map.of(" ", "value")));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SessionDraft("session-1", null, null, Map.of("key", " ")));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SessionMessageDraft(" ", SessionMessageRole.USER, "hello", Map.of()));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SessionMessageDraft("message-1", SessionMessageRole.USER, " ", Map.of()));
  }
}
