package com.codecli.observability;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Redacts credentials and bounds payloads before they leave the process. */
public final class TracePayloadSanitizer {
    private static final int DEFAULT_MAX = 8_000;
    private TracePayloadSanitizer() {}

    public static String text(Object value, int maxChars) {
        if (value == null) return null;
        String s = String.valueOf(value);
        s = s.replaceAll("(?i)Bearer +[^ \\\"'}]+", "Bearer ***");
        s = s.replaceAll("(?i)(\\\"?(?:token|key|password|secret|authorization)\\\"?)[=:]\\\"?[^,} \\\"]+\\\"?", "$1=***");
        int limit = maxChars <= 0 ? DEFAULT_MAX : maxChars;
        return s.length() <= limit ? s : s.substring(0, limit) + "...(truncated)";
    }

    public static String text(Object value) { return text(value, DEFAULT_MAX); }

    public static String sha256(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < 8 && i < digest.length; i++) out.append(String.format("%02x", digest[i]));
            return out.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
