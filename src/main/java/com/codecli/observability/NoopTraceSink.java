package com.codecli.observability;

import java.util.Map;
import java.util.UUID;

/** No-op implementation used when external tracing is disabled. */
public final class NoopTraceSink implements TraceSink {
    @Override public TraceHandle startTurn(String name, String input, Map<String, Object> metadata) {
        return new TraceHandle(UUID.randomUUID().toString().replace("-", ""), name, System.nanoTime());
    }
    @Override public TraceObservation startObservation(TraceHandle parent, String name, String kind, Map<String, Object> metadata) {
        return new TraceObservation(UUID.randomUUID().toString().replace("-", ""),
                parent == null ? "" : parent.traceId(), null,
                name, kind, System.nanoTime());
    }
    @Override public void endObservation(TraceObservation observation, String status, Object output, Throwable error) {}
    @Override public void endTurn(TraceHandle turn, String status, Object output, Throwable error) {}
    @Override public void score(String traceId, String name, double value, String dataType, Map<String, Object> metadata) {}
    @Override public boolean enabled() { return false; }
}
