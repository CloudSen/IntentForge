# intentforge-boot-server

Minimal server-side bootstrap entrypoint for the event-driven coding agent MVP.

Current implementation:
- JDK `HttpServer`
- HTTP JSON endpoints + SSE event stream
- request handling prefers virtual threads
- terminal demo main: `cn.intentforge.boot.server.AiAssetServerMain`

Demo startup:

```bash
cd /Users/clouds3n/Coding/open-source/ai/intent-forge

./mvnw -q -Drevision=nightly-SNAPSHOT \
  -pl intentforge-boot/intentforge-boot-server \
  -am \
  -DskipTests \
  package dependency:build-classpath \
  -Dmdep.outputFile=/tmp/intentforge-boot-server.cp

CLASSPATH="intentforge-boot/intentforge-boot-server/target/classes:$(cat /tmp/intentforge-boot-server.cp)"

java -Dintentforge.server.port=18080 \
  -cp "$CLASSPATH" \
  cn.intentforge.boot.server.AiAssetServerMain
```

Demo defaults printed on startup:
- base URL like `http://127.0.0.1:18080`
- demo `sessionId=session-1`
