package com.codecli.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractOpenAiCompatibleClientTruncationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void requestBodyIncludesDynamicMaxTokensAndStreamResponseCanBeMarkedTruncated() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                body.append("data: {\"choices\":[{\"delta\":{\"role\":\"assistant\",\"content\":\"")
                        .append("0123456789".repeat(20))
                        .append("\"}}],\"usage\":{\"prompt_tokens\":20,\"completion_tokens\":1}}\n\n");
            }
            body.append("data: [DONE]\n\n");
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "text/event-stream")
                    .setBody(body.toString()));
            TestClient client = new TestClient(server.url("/chat/completions").toString(), 1_024, 12);

            LlmClient.ChatResponse response = client.chat(List.of(LlmClient.Message.user("请简短回答")), null);
            RecordedRequest request = server.takeRequest();
            JsonNode root = MAPPER.readTree(request.getBody().readUtf8());

            assertEquals(12, root.path("max_tokens").asInt(), "max_tokens should be set from client guard");
            assertTrue(response.truncated(), "long streamed content should be marked as truncated");
            assertTrue(response.content().length() > 0, "client should still return partial content");
        }
    }

    @Test
    void streamListenerStillReceivesContentBeforeTruncation() throws Exception {
        try (MockWebServer server = new MockWebServer()) {
            server.enqueue(new MockResponse()
                    .setHeader("Content-Type", "text/event-stream")
                    .setBody("""
                            data: {"choices":[{"delta":{"role":"assistant","content":"short answer"}}],"usage":{"prompt_tokens":20,"completion_tokens":1}}

                            data: [DONE]

                            """));
            TestClient client = new TestClient(server.url("/chat/completions").toString(), 8_192, 256);
            AtomicInteger contentCalls = new AtomicInteger();

            LlmClient.ChatResponse response = client.chat(
                    List.of(LlmClient.Message.user("hello")),
                    null,
                    new LlmClient.StreamListener() {
                        @Override
                        public void onContentDelta(String delta) {
                            if (delta != null && !delta.isBlank()) {
                                contentCalls.incrementAndGet();
                            }
                        }
                    });

            assertEquals("short answer", response.content());
            assertTrue(contentCalls.get() > 0, "stream listener should receive content deltas");
        }
    }

    private static final class TestClient extends AbstractOpenAiCompatibleClient {
        private final String apiUrl;
        private final int maxWindow;
        private final int fixedMaxTokens;

        private TestClient(String apiUrl, int maxWindow, int fixedMaxTokens) {
            this.apiUrl = apiUrl;
            this.maxWindow = maxWindow;
            this.fixedMaxTokens = fixedMaxTokens;
        }

        @Override
        protected String getApiUrl() {
            return apiUrl;
        }

        @Override
        protected String getModel() {
            return "test-model";
        }

        @Override
        public String getModelName() {
            return getModel();
        }

        @Override
        public String getProviderName() {
            return "test";
        }

        @Override
        protected String getApiKey() {
            return "test-key";
        }

        @Override
        public int maxContextWindow() {
            return maxWindow;
        }

        @Override
        protected int computeMaxOutputTokens(int inputTokens) {
            return fixedMaxTokens;
        }
    }
}
