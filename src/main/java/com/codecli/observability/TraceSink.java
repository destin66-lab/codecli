package com.codecli.observability;

import java.util.Map;

/** Platform-neutral Agent tracing boundary. Implementations must be non-blocking for the caller. */
public interface TraceSink extends AutoCloseable {
    TraceHandle startTurn(String name, String input, Map<String, Object> metadata);
    TraceObservation startObservation(TraceHandle parent, String name, String kind, Map<String, Object> metadata);
    default TraceObservation startObservation(TraceObservation parent, String name, String kind,
                                               Map<String, Object> metadata) {
        return startObservation(parent == null ? null : new TraceHandle(parent.traceId(), parent.name(), parent.startNanos()),
                name, kind, metadata);
    }
    void endObservation(TraceObservation observation, String status, Object output, Throwable error);
    void endTurn(TraceHandle turn, String status, Object output, Throwable error);
    void score(String traceId, String name, double value, String dataType, Map<String, Object> metadata);

    default boolean enabled() { return true; }

    @Override
    default void close() {}

    record TraceHandle(String traceId, String name, long startNanos) {}
    record TraceObservation(String id, String traceId, String parentObservationId,
                            String name, String kind, long startNanos) {}
}
