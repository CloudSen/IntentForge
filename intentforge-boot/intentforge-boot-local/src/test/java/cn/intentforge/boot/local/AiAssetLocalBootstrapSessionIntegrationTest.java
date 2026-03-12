package cn.intentforge.boot.local;

import cn.intentforge.session.model.Session;
import cn.intentforge.session.model.SessionDraft;
import cn.intentforge.session.model.SessionMessageDraft;
import cn.intentforge.session.model.SessionMessageRole;
import cn.intentforge.session.model.SessionQuery;
import cn.intentforge.session.model.SessionStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AiAssetLocalBootstrapSessionIntegrationTest {
  @Test
  void shouldBootstrapWithSessionManager() throws Exception {
    Path pluginsDirectory = Files.createTempDirectory("boot-session-plugins");
    AiAssetLocalRuntime runtime = AiAssetLocalBootstrap.bootstrap(pluginsDirectory);

    Session created = runtime.sessionManager().create(new SessionDraft(
        " session-1 ",
        " Session Module ",
        " application-alpha ",
        Map.of(" topic ", " spi ")));
    Assertions.assertEquals("session-1", created.id());
    Assertions.assertEquals(SessionStatus.ACTIVE, created.status());

    Session appended = runtime.sessionManager().appendMessage(
        "session-1",
        new SessionMessageDraft(
            " message-1 ",
            SessionMessageRole.USER,
            "Need unified session management",
            Map.of(" source ", " boot ")));
    Assertions.assertEquals(1, appended.messages().size());
    Assertions.assertEquals(
        List.of(appended),
        runtime.sessionManager().list(new SessionQuery("application-alpha", SessionStatus.ACTIVE, "unified")));

    Session archived = runtime.sessionManager().archive("session-1");
    Assertions.assertEquals(SessionStatus.ARCHIVED, archived.status());

    runtime.sessionManager().delete("session-1");
    Assertions.assertTrue(runtime.sessionManager().find("session-1").isEmpty());
  }
}
