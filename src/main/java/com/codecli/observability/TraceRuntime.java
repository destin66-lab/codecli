package com.codecli.observability;

import java.util.Objects;

/** Thread-local trace context, propagated explicitly by ToolRegistry parallel workers. */
public final class TraceRuntime {
    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();
    private static volatile TraceSink DEFAULT = new NoopTraceSink();
    private TraceRuntime() {}

    public static TraceContext current() { return CURRENT.get(); }

    public static TraceSink defaultSink() { return DEFAULT; }

    public static void setDefaultSink(TraceSink sink) {
        DEFAULT = sink == null ? new NoopTraceSink() : sink;
    }

    public static Scope enter(TraceContext context) {
        TraceContext previous = CURRENT.get();
        if (context == null) CURRENT.remove(); else CURRENT.set(context);
        return () -> {
            if (previous == null) CURRENT.remove(); else CURRENT.set(previous);
        };
    }

    public record TraceContext(TraceSink sink, TraceSink.TraceHandle turn,
                               TraceSink.TraceObservation parent) {
        public TraceContext {
            Objects.requireNonNull(sink, "sink");
            Objects.requireNonNull(turn, "turn");
        }
        public TraceContext withParent(TraceSink.TraceObservation next) {
            return new TraceContext(sink, turn, next);
        }
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override void close();
    }
}
