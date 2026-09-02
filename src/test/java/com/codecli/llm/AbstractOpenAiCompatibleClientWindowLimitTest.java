package com.codecli.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractOpenAiCompatibleClientWindowLimitTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void requestBodyContainsMaxTokensComputedFromInputEstimate() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "text/event-stream")
                    .setBody("""
                            data: {"choices":[{"delta":{"role":"assistant","content":"ok"}}],"usage":{"prompt_tokens":10,"completion_tokens":1}}

                            data: [DONE]

                            """));
            TestClient client = new TestClient(server.url("/chat/completions").toString(), 128_000);

            client.chat(List.of(LlmClient.Message.user("hello world")), null);

            RecordedRequest request = server.takeRequest();
            JsonNode body = MAPPER.readTree(request.getBody().readUtf8());

            assertTrue(body.has("max_tokens"), "request should contain max_tokens sent from AbstractOpenAiCompatibleClient.computeMaxOutputTokens");
            int maxTokens = body.get("max_tokens").asInt();
            assertTrue(maxTokens >= 256, "max_tokens should have a sane lower bound");
            assertTrue(maxTokens <= 128_000, "max_tokens should not exceed window");
        }
    }

    @Test
    void maxTokensRespectsSmallRemainingWindow() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "text/event-stream")
                    .setBody("""
                            data: {"choices":[{"delta":{"role":"assistant","content":"ok"}}],"usage":{"prompt_tokens":10,"completion_tokens":1}}

                            data: [DONE]

                            """));
            // Tiny window + long input -> remaining window small, max_tokens should be capped by available
            String longInput = "a".repeat(6000);
            TestClient smallWindow = new TestClient(server.url("/chat/completions").toString(), 8_000);
            smallWindow.chat(List.of(LlmClient.Message.user(longInput)), null);

            RecordedRequest request = server.takeRequest();
            JsonNode body = MAPPER.readTree(request.getBody().readUtf8());
            int maxTokens = body.get("max_tokens").asInt();
            // 8000 window: reserve=2000, buffer=1000, cap=5000; input ~1500 tokens; available ~ 2500
            assertTrue(maxTokens >= 256);
            assertTrue(maxTokens <= 5000);
        }
    }

    @Test
    void streamingTruncationStopsAppendingContentAndMarksTruncated() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            // Each content delta ~ ~5 tokens under MemoryEntry estimate (20 chars / 4)
            String largeChunk = "a".repeat(40);
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "text/event-stream")
                    .setBody("""
                            data: {"choices":[{"delta":{"role":"assistant","content":"%s"}}]}

                            data: {"choices":[{"delta":{"content":"%s"}}]}

                            data: {"choices":[{"delta":{"content":"%s"}}]}

                            data: {"choices":[{"delta":{"content":"%s"}}],"usage":{"prompt_tokens":10,"completion_tokens":20}}

                            data: [DONE]

                            """.formatted(largeChunk, largeChunk, largeChunk, largeChunk)));

            // Force a tiny max_output so the 2nd-4th deltas trip truncation
            CappedClient client = new CappedClient(server.url("/chat/completions").toString(), 15);

            LlmClient.ChatResponse response = client.chat(List.of(LlmClient.Message.user("hi")), null);

            assertTrue(response.truncated(), "output exceeding max_output should be marked truncated");
            // First chunk kept, later chunks dropped
            assertTrue(response.content().length() < largeChunk.length() * 4,
                    "truncated content should be shorter than full stream");
            assertFalse(response.content().isEmpty());
        }
    }

    @Test
    void nonTruncatedStreamRemainsNotTruncated() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "text/event-stream")
                    .setBody("""
                            data: {"choices":[{"delta":{"role":"assistant","content":"short"}}],"usage":{"prompt_tokens":10,"completion_tokens":1}}

                            data: [DONE]

                            """));
            CappedClient client = new CappedClient(server.url("/chat/completions").toString(), 50_000);
            LlmClient.ChatResponse response = client.chat(List.of(LlmClient.Message.user("hi")), null);
            assertFalse(response.truncated());
            assertEquals("short", response.content());
        }
    }

    @Test
    void computeMaxOutputTokensIsBounded() {
        TestClient c128k = new TestClient("http://example.invalid", 128_000);
        assertTrue(c128k.exposedComputeMaxOutputTokens(100) > 256);
        assertTrue(c128k.exposedComputeMaxOutputTokens(100) <= 128_000);

        TestClient c8k = new TestClient("http://example.invalid", 8_000);
        int tiny = c8k.exposedComputeMaxOutputTokens(100);
        assertTrue(tiny >= 256);
    }

    @Test
    void chatResponseWithTruncatedPreservesFields() {
        LlmClient.ChatResponse base = new LlmClient.ChatResponse("assistant", "hello", null, null, 10, 5, 0, false);
        LlmClient.ChatResponse truncated = base.withTruncated(true);
        assertTrue(truncated.truncated());
        assertEquals("hello", truncated.content());
        assertEquals(10, truncated.inputTokens());
        assertFalse(base.truncated());
    }

    private static final class TestClient extends AbstractOpenAiCompatibleClient {
        private final String apiUrl;
        private final int window;

        private TestClient(String apiUrl, int window) {
            this.apiUrl = apiUrl;
            this.window = window;
        }

        @Override
        protected String getApiUrl() { return apiUrl; }

        @Override
        protected String getModel() { return "window-limit-test"; }

        @Override
        public String getModelName() { return getModel(); }

        @Override
        public String getProviderName() { return "test"; }

        @Override
        protected String getApiKey() { return "test-key"; }

        @Override
        public int maxContextWindow() { return window; }

        int exposedComputeMaxOutputTokens(int inputTokens) {
            return computeMaxOutputTokens(inputTokens);
        }
    }

    private static final class CappedClient extends AbstractOpenAiCompatibleClient {
        private final String apiUrl;
        private final int cap;

        private CappedClient(String apiUrl, int cap) {
            this.apiUrl = apiUrl;
            this.cap = cap;
        }

        @Override
        protected String getApiUrl() { return apiUrl; }

        @Override
        protected String getModel() { return "capped-test"; }

        @Override
        public String getModelName() { return getModel(); }

        @Override
        public String getProviderName() { return "test"; }

        @Override
        protected String getApiKey() { return "test-key"; }

        @Override
        protected int computeMaxOutputTokens(int inputTokens) {
            return cap;
        }
    }
}
