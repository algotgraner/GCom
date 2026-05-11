package se.gcom.app.debug;

import java.time.Instant;

public record DebugEvent(
        long sequenceNumber,
        Instant timestamp,
        DebugEventType type,
        String source,
        String details
) {
    public String toDisplayString() {
        return "#%d [%s] source=%s %s".formatted(
                sequenceNumber,
                type,
                valueOrDash(source),
                valueOrDash(details)
        );
    }

    private static String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
