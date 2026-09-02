package com.codecli.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class AsyncTraceExporter implements AutoCloseable {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final ObjectMapper mapper = new ObjectMapper();
    private final BlockingQueue<Object> queue;
    private final OkHttpClient client;
    private final String endpoint;
    private final String auth;
    private final int batchSize;
    private final long flushIntervalMillis;
    private final Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(true);

    AsyncTraceExporter(TraceConfig config) {
        this.queue = new ArrayBlockingQueue<>(config.queueCapacity());
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .callTimeout(15, TimeUnit.SECONDS)
                .build();
        this.endpoint = config.baseUrl() + "/api/public/ingestion";
        this.auth = Credentials.basic(config.publicKey(), config.secretKey());
        this.batchSize = config.batchSize();
        this.flushIntervalMillis = config.flushIntervalMillis();
        this.worker = new Thread(this::runLoop, "codecli-langfuse-exporter");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    void offer(Object event) {
        if (event != null) queue.offer(event);
    }

    private void runLoop() {
        List<Object> batch = new ArrayList<>(batchSize);
        while (running.get() || !queue.isEmpty()) {
            try {
                Object first = queue.poll(flushIntervalMillis, TimeUnit.MILLISECONDS);
                if (first != null) batch.add(first);
                queue.drainTo(batch, batchSize - batch.size());
                if (!batch.isEmpty() && (batch.size() >= batchSize || first == null)) {
                    send(batch);
                    batch.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
                batch.clear();
            }
        }
        if (!batch.isEmpty()) send(batch);
    }

    private void send(List<Object> events) {
        try {
            String body = mapper.writeValueAsString(Map.of("batch", events));
            Request request = new Request.Builder().url(endpoint)
                    .header("Authorization", auth)
                    .post(RequestBody.create(body, JSON)).build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) return;
            }
        } catch (IOException ignored) {
            // Observability is best-effort and must never affect Agent execution.
        }
    }

    @Override public void close() {
        if (!running.compareAndSet(true, false)) return;
        try { worker.join(Math.max(2_000, flushIntervalMillis + 500)); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); worker.interrupt(); }
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
