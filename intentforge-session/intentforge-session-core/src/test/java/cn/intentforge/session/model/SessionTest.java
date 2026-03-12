package cn.intentforge.session.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SessionTest {
  @Test
  void shouldNormalizeSessionAndMatchKeywordQueries() {
    Instant createdAt = Instant.parse("2026-03-12T07:32:00Z");
    Instant updatedAt = Instant.parse("2026-03-12T07:35:00Z");
    Session session = new Session(
        " session-1 ",
        " Planning Session ",
        " application-alpha ",
        null,
        List.of(new SessionMessage(
            " message-1 ",
            SessionMessageRole.USER,
            "Need a session SPI module",
            Map.of(" source ", " ui "),
            createdAt)),
        Map.of(" topic ", " design "),
        createdAt,
        updatedAt);

    Assertions.assertEquals("session-1", session.id());
    Assertions.assertEquals("Planning Session", session.title());
    Assertions.assertEquals("application-alpha", session.spaceId());
    Assertions.assertEquals(SessionStatus.ACTIVE, session.status());
    Assertions.assertEquals("message-1", session.messages().get(0).id());
    Assertions.assertEquals(Map.of("topic", "design"), session.metadata());
    Assertions.assertTrue(session.matches(new SessionQuery("application-alpha", SessionStatus.ACTIVE, "planning")));
    Assertions.assertTrue(session.matches(new SessionQuery(null, null, "session spi")));
    Assertions.assertFalse(session.matches(new SessionQuery("application-beta", null, null)));
    Assertions.assertFalse(session.matches(new SessionQuery(null, SessionStatus.ARCHIVED, null)));
    Assertions.assertThrows(UnsupportedOperationException.class, () -> session.messages().add(null));
  }

  @Test
  void shouldRejectInvalidSessionSnapshots() {
    Instant timestamp = Instant.parse("2026-03-12T07:32:00Z");
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new Session(" ", null, null, SessionStatus.ACTIVE, List.of(), Map.of(), timestamp, timestamp));
    Assertions.assertThrows(
        NullPointerException.class,
        () -> new Session("session-1", null, null, SessionStatus.ACTIVE, List.of(), Map.of(), null, timestamp));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SessionMessage("message-1", SessionMessageRole.USER, " ", Map.of(), timestamp));
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> new SessionQuery(" ", null, "keyword"));
  }
}
