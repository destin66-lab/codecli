package com.codecli.observability;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TracePayloadSanitizerTest {
    @Test void redactsCredentialsAndBoundsText() {
        String value = TracePayloadSanitizer.text("Bearer abc123 token=xyz password:secret", 20);
        assertFalse(value.contains("abc123"));
        assertFalse(value.contains("xyz"));
        assertTrue(value.contains("***"));
        assertTrue(value.endsWith("...(truncated)"));
    }
}
