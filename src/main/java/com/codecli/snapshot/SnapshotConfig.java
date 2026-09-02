package com.codecli.snapshot;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public record SnapshotConfig(
        boolean enabled,
        Path snapshotsRoot,
        int maxSnapshots,
        List<String> excludes
) {
    private static final List<String> DEFAULT_EXCLUDES = List.of(
            ".git",
            ".codecli/snapshots",
            "target",
            "node_modules",
            "dist",
            ".idea",
            "*.class",
            "*.jar"
    );

    public static SnapshotConfig fromEnvironment() {
        boolean enabled = readBoolean("codecli.snapshot.enabled", "CODECLI_SNAPSHOT_ENABLED",
                "codecli.snapshot.enabled", "CODECLI_SNAPSHOT_ENABLED", true);
        Path root = Path.of(readString("codecli.snapshot.dir", "CODECLI_SNAPSHOT_DIR",
                "codecli.snapshot.dir", "CODECLI_SNAPSHOT_DIR", defaultRoot().toString()));
        int max = readInt("codecli.snapshot.max", "CODECLI_SNAPSHOT_MAX",
                "codecli.snapshot.max", "CODECLI_SNAPSHOT_MAX", 50);
        List<String> excludes = mergeExcludes(readString("codecli.snapshot.excludes", "CODECLI_SNAPSHOT_EXCLUDES",
                "codecli.snapshot.excludes", "CODECLI_SNAPSHOT_EXCLUDES", ""));
        return new SnapshotConfig(enabled, root, Math.max(1, max), excludes);
    }

    public SnapshotConfig withEnabled(boolean enabled) {
        return new SnapshotConfig(enabled, snapshotsRoot, maxSnapshots, excludes);
    }

    private static Path defaultRoot() {
        Path home = Path.of(System.getProperty("user.home"));
        Path modern = home.resolve(".codecli/snapshots");
        Path legacy = home.resolve(".codecli/snapshots");
        return java.nio.file.Files.exists(modern) || !java.nio.file.Files.exists(legacy) ? modern : legacy;
    }

    private static boolean readBoolean(String modernProperty, String modernEnv,
                                       String legacyProperty, String legacyEnv, boolean fallback) {
        String value = readNullable(modernProperty, modernEnv, legacyProperty, legacyEnv);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "1", "true", "yes", "on" -> true;
            case "0", "false", "no", "off" -> false;
            default -> fallback;
        };
    }

    private static int readInt(String modernProperty, String modernEnv,
                               String legacyProperty, String legacyEnv, int fallback) {
        String value = readNullable(modernProperty, modernEnv, legacyProperty, legacyEnv);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static String readString(String modernProperty, String modernEnv,
                                     String legacyProperty, String legacyEnv, String fallback) {
        String value = readNullable(modernProperty, modernEnv, legacyProperty, legacyEnv);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String readNullable(String modernProperty, String modernEnv,
                                      String legacyProperty, String legacyEnv) {
        String value = System.getProperty(modernProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(modernEnv);
        }
        if (value == null || value.isBlank()) {
            value = System.getProperty(legacyProperty);
        }
        if (value == null || value.isBlank()) {
            value = System.getenv(legacyEnv);
        }
        return value;
    }

    private static List<String> mergeExcludes(String configured) {
        Set<String> merged = new LinkedHashSet<>(DEFAULT_EXCLUDES);
        if (configured != null && !configured.isBlank()) {
            for (String item : configured.split(",")) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    merged.add(trimmed);
                }
            }
        }
        return new ArrayList<>(merged);
    }
}
