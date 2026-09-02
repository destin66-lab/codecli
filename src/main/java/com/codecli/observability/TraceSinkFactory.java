package com.codecli.observability;

public final class TraceSinkFactory {
    private TraceSinkFactory() {}

    public static TraceSink create() {
        TraceConfig config = TraceConfig.load();
        if (!config.enabled()) return new NoopTraceSink();
        if (config.sampleRate() < 1.0 && java.util.concurrent.ThreadLocalRandom.current().nextDouble() > config.sampleRate()) {
            return new NoopTraceSink();
        }
        return new LangfuseTraceSink(config);
    }
}
