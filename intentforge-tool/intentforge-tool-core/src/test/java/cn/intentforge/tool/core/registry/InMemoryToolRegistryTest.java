package cn.intentforge.tool.core.registry;

import cn.intentforge.tool.core.model.ToolCallResult;
import cn.intentforge.tool.core.model.ToolDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InMemoryToolRegistryTest {
  @Test
  void shouldRegisterAndOverrideAndUnregister() {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    ToolDefinition definition = new ToolDefinition("tool.a", "a", java.util.Map.of(), false);
    ToolRegistration registrationA = new ToolRegistration(definition, request -> ToolCallResult.success("A"));
    ToolRegistration registrationB = new ToolRegistration(definition, request -> ToolCallResult.success("B"));

    registry.register(registrationA);
    Assertions.assertEquals(1, registry.list().size());
    Assertions.assertEquals("A", registry.find("tool.a").orElseThrow().handler().handle(null).output());

    registry.register(registrationB);
    Assertions.assertEquals(1, registry.list().size());
    Assertions.assertEquals("B", registry.find("tool.a").orElseThrow().handler().handle(null).output());

    registry.unregister("tool.a");
    Assertions.assertTrue(registry.find("tool.a").isEmpty());
  }

  @Test
  void shouldSupportConcurrentRegistration() throws Exception {
    InMemoryToolRegistry registry = new InMemoryToolRegistry();
    int taskCount = 20;
    CountDownLatch latch = new CountDownLatch(taskCount);
    try (var executor = Executors.newFixedThreadPool(4)) {
      for (int index = 0; index < taskCount; index++) {
        int current = index;
        executor.submit(() -> {
          try {
            ToolDefinition definition = new ToolDefinition("tool." + current, "d", java.util.Map.of(), false);
            registry.register(new ToolRegistration(definition, request -> ToolCallResult.success("ok")));
          } finally {
            latch.countDown();
          }
        });
      }
      Assertions.assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    List<String> ids = new ArrayList<>();
    registry.list().forEach(item -> ids.add(item.definition().id()));
    Assertions.assertEquals(taskCount, ids.size());
  }
}
