package cn.intentforge.session.local.registry;

import cn.intentforge.session.SessionNotFoundException;
import cn.intentforge.session.model.Session;
import cn.intentforge.session.model.SessionDraft;
import cn.intentforge.session.model.SessionMessageDraft;
import cn.intentforge.session.model.SessionMessageRole;
import cn.intentforge.session.model.SessionQuery;
import cn.intentforge.session.model.SessionStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InMemorySessionManagerTest {
  @Test
  void shouldCreateAppendArchiveQueryAndDeleteSessions() {
    InMemorySessionManager manager = new InMemorySessionManager(
        Thread.currentThread().getContextClassLoader(),
        Clock.fixed(Instant.parse("2026-03-12T07:40:00Z"), ZoneOffset.UTC));

    Session created = manager.create(new SessionDraft(
        " session-1 ",
        " Planning Session ",
        " application-alpha ",
        Map.of(" topic ", " session ")));
    Assertions.assertEquals(SessionStatus.ACTIVE, created.status());
    Assertions.assertEquals(List.of(), created.messages());

    Session appended = manager.appendMessage(
        "session-1",
        new SessionMessageDraft(
            " message-1 ",
            SessionMessageRole.USER,
            "Need boot-local session integration",
            Map.of(" source ", " ui ")));
    Assertions.assertEquals(1, appended.messages().size());
    Assertions.assertEquals("message-1", appended.messages().get(0).id());
    Assertions.assertEquals(
        List.of(appended),
        manager.list(new SessionQuery("application-alpha", SessionStatus.ACTIVE, "boot-local")));

    Session archived = manager.archive("session-1");
    Assertions.assertEquals(SessionStatus.ARCHIVED, archived.status());
    Assertions.assertEquals(List.of(archived), manager.list(new SessionQuery(null, SessionStatus.ARCHIVED, null)));

    manager.delete("session-1");
    Assertions.assertTrue(manager.find("session-1").isEmpty());
  }

  @Test
  void shouldRejectDuplicateAndMissingSessionsWhileToleratingBlankLookup() {
    InMemorySessionManager manager = new InMemorySessionManager(
        Thread.currentThread().getContextClassLoader(),
        Clock.fixed(Instant.parse("2026-03-12T07:40:00Z"), ZoneOffset.UTC));

    manager.create(new SessionDraft("session-1", null, null, Map.of()));

    Assertions.assertThrows(
        IllegalStateException.class,
        () -> manager.create(new SessionDraft("session-1", "duplicate", null, Map.of())));
    Assertions.assertThrows(
        SessionNotFoundException.class,
        () -> manager.appendMessage(
            "missing",
            new SessionMessageDraft("message-1", SessionMessageRole.USER, "hello", Map.of())));
    Assertions.assertThrows(SessionNotFoundException.class, () -> manager.archive("missing"));
    Assertions.assertTrue(manager.find(" ").isEmpty());

    manager.delete(" ");
    Assertions.assertEquals(1, manager.list(null).size());
  }
}
