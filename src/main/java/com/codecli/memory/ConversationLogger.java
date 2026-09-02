package com.codecli.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.codecli.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 对话历史落盘器 - 把 ReAct 主循环的 {@code conversationHistory} 增量追加写入 JSONL。
 *
 * 与短期记忆的关系：
 * - {@link ConversationMemory} 是内存上下文（受 token 预算约束，压缩会删除旧消息）；
 *   本类只是把"即将被压缩删除的消息"持久化成可回溯的副本，不占上下文、不触发压缩。
 * - system 消息（含 PAI.md 项目指令）每次运行都从磁盘重建，不落盘，避免膨胀。
 * - 图片 base64 是体积大头，落盘时用占位文本省略。
 *
 * 设计要点：
 * 1. 按项目分目录、按会话分文件：{sessionsRoot}/{projectHash}/session-{ts}.jsonl
 * 2. 增量写入：{@link #sync} 用消息指纹去重，压缩重建列表后不会重复写尾部保留消息
 * 3. 保留策略：构造时清理超过 {@code retentionDays} 的会话文件（默认 7 天）
 */
public class ConversationLogger {
    private static final Logger log = LoggerFactory.getLogger(ConversationLogger.class);

    private static final String STORAGE_DIR_PROPERTY = "codecli.session.dir";
    private static final String STORAGE_DIR_ENV = "CODECLI_SESSION_DIR";
    private static final String MODERN_STORAGE_DIR_PROPERTY = "codecli.session.dir";
    private static final String MODERN_STORAGE_DIR_ENV = "CODECLI_SESSION_DIR";

    private static final String SESSION_PREFIX = "session-";
    private static final String SESSION_SUFFIX = ".jsonl";
    private static final DateTimeFormatter SESSION_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final int DEFAULT_RETENTION_DAYS = 7;

    private final ObjectMapper mapper = new ObjectMapper();
    private final File sessionFile;
    private final Set<String> writtenFingerprints = new HashSet<>();
    private int writtenCount;

    public ConversationLogger(String projectPath) {
        this(resolveStorageDir(), projectPath, DEFAULT_RETENTION_DAYS);
    }

    public ConversationLogger(File storageDir, String projectPath) {
        this(storageDir, projectPath, DEFAULT_RETENTION_DAYS);
    }

    public ConversationLogger(File storageDir, String projectPath, int retentionDays) {
        File dir = storageDir;
        if (dir == null) {
            dir = resolveStorageDir();
        }
        File projectDir = new File(dir, projectKeyOf(projectPath));
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            log.warn("无法创建会话目录: {}", projectDir);
        }
        this.sessionFile = new File(projectDir,
                SESSION_PREFIX + SESSION_TS.format(LocalDateTime.now()) + SESSION_SUFFIX);
        cleanupOldSessions(dir, retentionDays);
    }

    /**
     * 增量落盘：把 history 中尚未写入的消息追加到当前会话文件。
     * 跳过 system 消息；幂等，可反复调用。
     */
    public synchronized void sync(List<LlmClient.Message> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        try {
            StringBuilder batch = new StringBuilder();
            for (LlmClient.Message message : history) {
                if (message == null || "system".equals(message.role())) {
                    continue;
                }
                LlmClient.Message serializable = message.hasImageContent()
                        ? message.withoutImageContent()
                        : message;
                String line;
                try {
                    line = mapper.writeValueAsString(serializable);
                } catch (IOException e) {
                    log.debug("序列化消息失败，跳过: {}", e.getMessage());
                    continue;
                }
                String fingerprint = shortSha256(line);
                if (!writtenFingerprints.add(fingerprint)) {
                    continue;
                }
                batch.append(line).append('\n');
            }
            if (!batch.isEmpty()) {
                Files.write(sessionFile.toPath(), batch.toString().getBytes(StandardCharsets.UTF_8),
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND);
                writtenCount++;
            }
        } catch (IOException e) {
            log.warn("对话历史落盘失败: {}", e.getMessage());
        }
    }

    public int getWrittenCount() {
        return writtenCount;
    }

    public File getSessionFile() {
        return sessionFile;
    }

    /** 删除超过保留天数的历史会话文件（只清 sessions 根目录下的 *.jsonl）。 */
    private void cleanupOldSessions(File storageDir, int retentionDays) {
        if (storageDir == null || !storageDir.isDirectory() || retentionDays <= 0) {
            return;
        }
        long cutoff = System.currentTimeMillis() - Duration.ofDays(retentionDays).toMillis();
        try (Stream<Path> walk = Files.walk(storageDir.toPath())) {
            walk.filter(path -> path.toString().endsWith(SESSION_SUFFIX))
                    .filter(path -> path.toFile().lastModified() < cutoff)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                            log.info("清理过期会话文件: {}", path);
                        } catch (IOException e) {
                            log.debug("清理会话文件失败: {}", e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.debug("扫描会话目录失败: {}", e.getMessage());
        }
    }

    private static File resolveStorageDir() {
        String configuredDir = firstNonBlank(
                System.getProperty(MODERN_STORAGE_DIR_PROPERTY),
                System.getenv(MODERN_STORAGE_DIR_ENV),
                System.getProperty(STORAGE_DIR_PROPERTY),
                System.getenv(STORAGE_DIR_ENV));
        if (configuredDir != null) {
            return new File(configuredDir);
        }
        return new File(System.getProperty("user.home"), ".codecli/sessions");
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String projectKeyOf(String projectPath) {
        String normalized = projectPath == null || projectPath.isBlank()
                ? "default"
                : Path.of(projectPath).toAbsolutePath().normalize().toString();
        return "project-" + shortSha256(normalized);
    }

    private static String shortSha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                        .append(Character.forDigit(b & 0xF, 16));
            }
            return sb.substring(0, 12);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(value.hashCode());
        }
    }
}
