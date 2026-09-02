package com.codecli.observability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public record TraceConfig(boolean enabled, String baseUrl, String publicKey, String secretKey,
                          String captureMode, double sampleRate, int maxContentChars,
                          int queueCapacity, int batchSize, int flushIntervalMillis) {
    public static TraceConfig load() {
        boolean enabled = bool(value("LANGFUSE_ENABLED", "false"));
        String baseUrl = value("LANGFUSE_BASE_URL", "https://cloud.langfuse.com");
        String publicKey = value("LANGFUSE_PUBLIC_KEY", "");
        String secretKey = value("LANGFUSE_SECRET_KEY", "");
        String captureMode = value("LANGFUSE_CAPTURE_MODE", "metadata").toLowerCase(Locale.ROOT);
        if (!captureMode.equals("off") && !captureMode.equals("metadata") && !captureMode.equals("full")) {
            captureMode = "metadata";
        }
        double sample = number(value("LANGFUSE_SAMPLE_RATE", "1.0"), 1.0);
        int max = integer(value("LANGFUSE_MAX_CONTENT_CHARS", "8000"), 8000);
        int capacity = integer(value("LANGFUSE_QUEUE_CAPACITY", "2048"), 2048);
        int batch = integer(value("LANGFUSE_BATCH_SIZE", "20"), 20);
        int flush = integer(value("LANGFUSE_FLUSH_INTERVAL_MS", "1000"), 1000);
        return new TraceConfig(enabled && !publicKey.isBlank() && !secretKey.isBlank(),
                trimSlash(baseUrl), publicKey, secretKey, captureMode,
                Math.max(0, Math.min(1, sample)), Math.max(256, max),
                Math.max(64, capacity), Math.max(1, batch), Math.max(100, flush));
    }

    private static String value(String key, String fallback) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return stripQuotes(sys.trim());
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return stripQuotes(env.trim());
        for (Path file : new Path[]{Path.of(".env"), Path.of(System.getProperty("user.home"), ".env")}) {
            if (!Files.exists(file)) continue;
            try {
                for (String line : Files.readAllLines(file)) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith(key + "=")) {
                        return stripQuotes(trimmed.substring(key.length() + 1).trim());
                    }
                }
            } catch (IOException ignored) {}
        }
        return fallback;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
    private static boolean bool(String value) { return Boolean.parseBoolean(value); }
    private static int integer(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (NumberFormatException e) { return fallback; }
    }
    private static double number(String value, double fallback) {
        try { return Double.parseDouble(value); } catch (NumberFormatException e) { return fallback; }
    }
    private static String trimSlash(String value) {
        if (value == null || value.isBlank()) return "https://cloud.langfuse.com";
        return value.replaceAll("/+$", "");
    }
}
