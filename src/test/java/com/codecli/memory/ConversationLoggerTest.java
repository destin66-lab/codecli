package com.codecli.memory;

import com.codecli.llm.LlmClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConversationLoggerTest {

    @TempDir
    Path tempDir;

    @Test
    void writesNewMessagesAndSkipsSystem() throws IOException {
        ConversationLogger logger = new ConversationLogger(tempDir.toFile(), "/repo/demo");
        List<LlmClient.Message> history = new ArrayList<>();
        history.add(LlmClient.Message.system("SYSTEM_PROMPT 不应落盘"));
        history.add(LlmClient.Message.user("你好"));
        history.add(LlmClient.Message.assistant("你好，有什么可以帮你？"));

        logger.sync(history);

        List<String> lines = Files.readAllLines(logger.getSessionFile().toPath(), StandardCharsets.UTF_8);
        assertEquals(2, lines.size());
        assertTrue(lines.get(0).contains("\"你好\""));
        assertFalse(lines.get(0).contains("SYSTEM_PROMPT"));
    }

    @Test
    void syncIsIdempotent() throws IOException {
        ConversationLogger logger = new ConversationLogger(tempDir.toFile(), "/repo/demo");
        List<LlmClient.Message> history = new ArrayList<>();
        history.add(LlmClient.Message.user("第一次输入"));
        history.add(LlmClient.Message.assistant("第一次回复"));

        logger.sync(history);
        logger.sync(history);  // 重复同步不应重复写
        logger.sync(new ArrayList<>(history.subList(1, 2)));  // 部分重复也不应重复写

        List<String> lines = Files.readAllLines(logger.getSessionFile().toPath(), StandardCharsets.UTF_8);
        assertEquals(2, lines.size());
    }

    @Test
    void omitsImageBase64Payloads() throws IOException {
        ConversationLogger logger = new ConversationLogger(tempDir.toFile(), "/repo/demo");
        String base64Payload = "QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo=";
        List<LlmClient.Message> history = new ArrayList<>();
        history.add(LlmClient.Message.user(List.of(
                LlmClient.ContentPart.text("看图说话"),
                LlmClient.ContentPart.imageBase64(base64Payload, "image/png")
        )));

        logger.sync(history);

        String content = Files.readString(logger.getSessionFile().toPath(), StandardCharsets.UTF_8);
        assertFalse(content.contains(base64Payload), "图片 base64 不应落盘");
        assertTrue(content.contains("图片附件已省略"));
    }

    @Test
    void cleanupRemovesExpiredSessionFiles() throws IOException {
        Path projectDir = tempDir.resolve("project-abc");
        Files.createDirectories(projectDir);
        Path oldFile = projectDir.resolve("session-old.jsonl");
        Files.writeString(oldFile, "{}\n");
        Files.setLastModifiedTime(oldFile, FileTime.fromMillis(System.currentTimeMillis() - 3L * 24 * 3600 * 1000));

        Path freshFile = projectDir.resolve("session-fresh.jsonl");
        Files.writeString(freshFile, "{}\n");

        new ConversationLogger(tempDir.toFile(), "/repo/demo", 1);

        assertFalse(Files.exists(oldFile), "超过保留天数的旧文件应被清理");
        assertTrue(Files.exists(freshFile));
    }

    @Test
    void roundTripSerializationOfToolCalls() throws IOException {
        ConversationLogger logger = new ConversationLogger(tempDir.toFile(), "/repo/demo");
        List<LlmClient.Message> history = new ArrayList<>();
        LlmClient.ToolCall toolCall = new LlmClient.ToolCall("call_1",
                new LlmClient.ToolCall.Function("read_file", "{\"path\":\"src/Main.java\"}"));
        history.add(LlmClient.Message.assistant("我来读文件", List.of(toolCall)));

        logger.sync(history);

        String line = Files.readString(logger.getSessionFile().toPath(), StandardCharsets.UTF_8).trim();
        assertTrue(line.contains("read_file"));
        assertTrue(line.contains("call_1"));
        assertTrue(line.contains("src/Main.java"));
    }
}
