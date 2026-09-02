package com.codecli.memory;

import com.codecli.llm.LlmClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class ContextCompressorTest {

    @TempDir
    Path tempDir;

    @Test
    void compressExtractsFactsIntoLongTermMemory() {
        FakeLlmClient llm = new FakeLlmClient(
                "用户偏好使用中文交流\n项目使用Java 17",  // extractFacts 响应
                "旧对话摘要S1"                            // map 摘要响应
        );
        ContextCompressor compressor = new ContextCompressor(llm, 3);
        LongTermMemory longTermMemory = new LongTermMemory(tempDir.toFile());
        ConversationMemory memory = conversationMemory(4, 50);

        compressor.compress(memory, longTermMemory);

        List<MemoryEntry> facts = longTermMemory.getByType(MemoryEntry.MemoryType.FACT);
        assertEquals(2, facts.size(), "压缩时应把稳定事实存入长期记忆");
        assertTrue(facts.stream().anyMatch(e -> e.getContent().contains("用户偏好使用中文交流")));
        assertTrue(facts.stream().anyMatch(e -> e.getContent().contains("项目使用Java 17")));
        assertTrue(memory.getAll().stream().anyMatch(e -> e.getType() == MemoryEntry.MemoryType.SUMMARY));
    }

    @Test
    void compressWithoutLongTermMemoryKeepsOldBehavior() {
        FakeLlmClient llm = new FakeLlmClient("旧对话摘要S1");
        ContextCompressor compressor = new ContextCompressor(llm, 3);
        ConversationMemory memory = conversationMemory(4, 50);

        String summary = compressor.compress(memory);

        assertNotNull(summary);
        assertEquals("旧对话摘要S1", summary);
        assertEquals(1, llm.userPrompts.size(), "无 longTermMemory 时不应触发事实提取");
    }

    @Test
    void secondCompressionMergesPreviousSummaryInsteadOfResummarizing() {
        // 第一次压缩：extract(1) + map(1)。6 条消息 retain=3 → 旧消息 3 条，恰好一个 map chunk。
        FakeLlmClient llm = new FakeLlmClient(
                "用户偏好使用中文",   // 第一次 extract
                "第一版摘要",          // 第一次 map
                "项目使用Java 17",     // 第二次 extract
                "第二版摘要",          // 第二次 map
                "合并后的完整摘要"     // 第二次 update
        );
        ContextCompressor compressor = new ContextCompressor(llm, 3);
        LongTermMemory longTermMemory = new LongTermMemory(tempDir.toFile());
        ConversationMemory memory = conversationMemory(3, 50);

        compressor.compress(memory, longTermMemory);
        assertTrue(memory.getAll().stream().anyMatch(e ->
                e.getType() == MemoryEntry.MemoryType.SUMMARY
                        && e.getContent().contains("第一版摘要")));

        // 新增 3 条消息后再压缩
        for (int i = 0; i < 3; i++) {
            memory.store(new MemoryEntry("u-" + i, "新增消息" + i, MemoryEntry.MemoryType.CONVERSATION,
                    Map.of("source", "user"), 10));
        }
        compressor.compress(memory, longTermMemory);

        // update 阶段被调用：最后一次请求包含上一轮摘要
        assertTrue(llm.userPrompts.get(llm.userPrompts.size() - 1).contains("=== 上一轮摘要 ==="),
                "第二次压缩应基于上一轮摘要做增量合并");
        assertTrue(llm.userPrompts.get(llm.userPrompts.size() - 1).contains("第一版摘要"));

        // 上一轮摘要不应再被当作普通历史重压
        boolean mapReusedOldSummary = llm.userPrompts.stream()
                .filter(p -> p.contains("对话片段"))
                .anyMatch(p -> p.contains("第一版摘要"));
        assertFalse(mapReusedOldSummary, "旧摘要不应再进入 map 摘要的输入");

        // 新摘要内容 = 合并结果
        assertTrue(memory.getAll().stream().anyMatch(e ->
                e.getType() == MemoryEntry.MemoryType.SUMMARY
                        && e.getContent().contains("合并后的完整摘要")));
    }

    @Test
    void extractFactsFiltersEphemeralLines() {
        FakeLlmClient llm = new FakeLlmClient(
                "用户想创建一个文件\n用户偏好使用中文",  // 第一条应被过滤
                "旧对话摘要S1"
        );
        ContextCompressor compressor = new ContextCompressor(llm, 3);
        LongTermMemory longTermMemory = new LongTermMemory(tempDir.toFile());
        ConversationMemory memory = conversationMemory(4, 50);

        compressor.compress(memory, longTermMemory);

        List<MemoryEntry> facts = longTermMemory.getByType(MemoryEntry.MemoryType.FACT);
        assertEquals(1, facts.size());
        assertTrue(facts.get(0).getContent().contains("用户偏好使用中文"));
    }

    private static ConversationMemory conversationMemory(int rounds, int tokenEach) {
        ConversationMemory memory = new ConversationMemory(100_000);
        for (int i = 0; i < rounds; i++) {
            memory.store(new MemoryEntry("user-" + i, "用户问题" + i + " " + "x".repeat(tokenEach),
                    MemoryEntry.MemoryType.CONVERSATION, Map.of("source", "user"), tokenEach));
            memory.store(new MemoryEntry("assistant-" + i, "助手回答" + i + " " + "y".repeat(tokenEach),
                    MemoryEntry.MemoryType.CONVERSATION, Map.of("source", "assistant"), tokenEach));
        }
        return memory;
    }

    /** 记录收到的 prompt，并按队列顺序返回预设响应。 */
    private static final class FakeLlmClient implements LlmClient {
        private final Queue<String> responses;
        final List<String> userPrompts = new ArrayList<>();

        FakeLlmClient(String... responses) {
            this.responses = new ArrayDeque<>(List.of(responses));
        }

        @Override
        public ChatResponse chat(List<Message> messages, List<Tool> tools) throws IOException {
            return chat(messages, tools, StreamListener.NO_OP);
        }

        @Override
        public ChatResponse chat(List<Message> messages, List<Tool> tools, StreamListener listener) {
            for (Message message : messages) {
                if ("user".equals(message.role()) && message.content() != null && !message.content().isBlank()) {
                    userPrompts.add(message.content());
                }
            }
            String next = responses.poll();
            return new ChatResponse("assistant", next == null ? "" : next, null, 0, 0);
        }

        @Override
        public String getModelName() {
            return "fake";
        }

        @Override
        public String getProviderName() {
            return "fake";
        }
    }
}
