package com.codecli.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class LangfuseTraceSink implements TraceSink {
    private final TraceConfig config;
    private final AsyncTraceExporter exporter;
    private final ObjectMapper mapper = new ObjectMapper();

    public LangfuseTraceSink(TraceConfig config) {
        this.config = config;
        this.exporter = new AsyncTraceExporter(config);
    }

    @Override public TraceHandle startTurn(String name, String input, Map<String, Object> metadata) {
        String id = id();
        long now = System.nanoTime();
        ObjectNode body = mapper.createObjectNode();
        body.put("id", id);
        body.put("name", name == null ? "agent.turn" : name);
        body.put("timestamp", Instant.now().toString());
        if (input != null && !"metadata".equals(config.captureMode())) body.put("input", safe(input));
        if (metadata != null) body.set("metadata", mapper.valueToTree(metadata));
        exporter.offer(envelope("trace-create", id, body));
        return new TraceHandle(id, name, now);
    }

    @Override public TraceObservation startObservation(TraceHandle parent, String name, String kind,
                                                        Map<String, Object> metadata) {
        String id = id();
        long now = System.nanoTime();
        String traceId = parent == null ? id : parent.traceId();
        // A trace handle identifies the root trace, not an observation. Root generations have no parent observation.
        String parentId = null;
        exporter.offer(envelope("generation".equals(kind) ? "generation-create" : "span-create", id,
                observationBody(id, traceId, parentId, name, kind, metadata)));
        return new TraceObservation(id, traceId, parentId, name, kind, now);
    }

    @Override public TraceObservation startObservation(TraceObservation parent, String name, String kind,
                                                        Map<String, Object> metadata) {
        String id = id();
        long now = System.nanoTime();
        String traceId = parent == null ? id : parent.traceId();
        String parentId = parent == null ? null : parent.id();
        exporter.offer(envelope("generation".equals(kind) ? "generation-create" : "span-create", id,
                observationBody(id, traceId, parentId, name, kind, metadata)));
        return new TraceObservation(id, traceId, parentId, name, kind, now);
    }

    @Override public void endObservation(TraceObservation observation, String status, Object output, Throwable error) {
        if (observation == null) return;
        String type = "generation".equals(observation.kind()) ? "generation-update" : "span-update";
        ObjectNode body = mapper.createObjectNode();
        body.put("id", observation.id());
        body.put("traceId", observation.traceId());
        if (observation.parentObservationId() != null) body.put("parentObservationId", observation.parentObservationId());
        body.put("name", observation.name() == null ? observation.kind() : observation.name());
        body.put("endTime", Instant.now().toString());
        if (status != null) body.put("statusMessage", status);
        if (output != null && !"metadata".equals(config.captureMode())) body.put("output", safe(output));
        if (error != null) body.put("error", safe(error.getMessage()));
        exporter.offer(envelope(type, observation.id(), body));
    }

    @Override public void endTurn(TraceHandle turn, String status, Object output, Throwable error) {
        if (turn == null) return;
        ObjectNode body = mapper.createObjectNode();
        body.put("id", turn.traceId());
        body.put("name", turn.name() == null ? "agent.turn" : turn.name());
        body.put("endTime", Instant.now().toString());
        if (status != null) body.put("statusMessage", status);
        if (output != null && !"metadata".equals(config.captureMode())) body.put("output", safe(output));
        if (error != null) body.put("error", safe(error.getMessage()));
        exporter.offer(envelope("trace-update", turn.traceId(), body));
    }

    @Override public void score(String traceId, String name, double value, String dataType,
                                Map<String, Object> metadata) {
        if (traceId == null || name == null) return;
        ObjectNode body = mapper.createObjectNode();
        body.put("id", id());
        body.put("traceId", traceId);
        body.put("name", name);
        body.put("value", value);
        body.put("dataType", dataType == null ? "NUMERIC" : dataType);
        if (metadata != null) body.set("metadata", mapper.valueToTree(metadata));
        exporter.offer(envelope("score-create", id(), body));
    }

    @Override public void close() { exporter.close(); }

    private ObjectNode observationBody(String id, String traceId, String parentId, String name, String kind,
                                       Map<String, Object> metadata) {
        ObjectNode body = mapper.createObjectNode();
        body.put("id", id);
        body.put("traceId", traceId);
        if (parentId != null) body.put("parentObservationId", parentId);
        body.put("name", name == null ? kind : name);
        body.put("startTime", Instant.now().toString());
        if (metadata != null) body.set("metadata", mapper.valueToTree(metadata));
        return body;
    }

    private ObjectNode envelope(String type, String id, ObjectNode body) {
        ObjectNode event = mapper.createObjectNode();
        event.put("id", id);
        event.put("type", type);
        event.put("timestamp", Instant.now().toString());
        event.set("body", body);
        return event;
    }

    private String safe(Object value) {
        return TracePayloadSanitizer.text(value, config.maxContentChars());
    }

    private static String id() {
        return UUID.randomUUID().toString().replace("-", "") + Long.toHexString(ThreadLocalRandom.current().nextLong());
    }
}
