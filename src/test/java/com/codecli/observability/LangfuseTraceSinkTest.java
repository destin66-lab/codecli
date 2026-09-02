package com.codecli.observability;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class LangfuseTraceSinkTest {
    @Test void emitsLangfuseEnvelopeWithParentObservation() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse().setResponseCode(200));
            server.start();
            TraceConfig config = new TraceConfig(true, server.url("/").toString(), "pk", "sk",
                    "full", 1.0, 8000, 64, 20, 100);
            try (LangfuseTraceSink sink = new LangfuseTraceSink(config)) {
                TraceSink.TraceHandle turn = sink.startTurn("turn", "hello", Map.of("mode", "test"));
                TraceSink.TraceObservation generation = sink.startObservation(turn, "gen", "generation", Map.of());
                TraceSink.TraceObservation tool = sink.startObservation(generation, "tool", "span", Map.of());
                sink.endObservation(tool, "completed", "ok", null);
                sink.endObservation(generation, "completed", "ok", null);
                sink.endTurn(turn, "completed", "ok", null);
            }
            var request = server.takeRequest(3, TimeUnit.SECONDS);
            assertNotNull(request);
            String body = request.getBody().readUtf8();
            assertTrue(body.contains("\"type\":\"generation-create\""));
            assertTrue(body.contains("\"parentObservationId\""));
            assertTrue(body.contains("\"body\""));
            assertTrue(request.getHeader("Authorization").startsWith("Basic "));
        }
    }
}
