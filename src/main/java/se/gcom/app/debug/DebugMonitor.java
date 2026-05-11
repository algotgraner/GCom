package se.gcom.app.debug;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class DebugMonitor {

    private final AtomicLong sequence = new AtomicLong();
    private final ObservableList<DebugEvent> events = FXCollections.observableArrayList();

    public ObservableList<DebugEvent> getEvents() {
        return events;
    }

    public void recordEvent(DebugEventType type, String source, String details) {
        DebugEvent event = new DebugEvent(
                sequence.incrementAndGet(),
                Instant.now(),
                type,
                source,
                details
        );

        if (Platform.isFxApplicationThread()) {
            events.add(event);
        } else {
            Platform.runLater(() -> events.add(event));
        }
    }

    public void addMockEvents() {
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Process started on localhost:5001");
        recordEvent(DebugEventType.MESSAGE_CREATED, "debug-mock", "Created message P1--1 in group Group1");
        recordEvent(DebugEventType.MESSAGE_DELIVERED, "debug-mock", "Delivered message P1--1 to Group1");
        recordEvent(DebugEventType.MESSAGE_CREATED, "debug-mock", "Created message P2--1 in group Group1");
        recordEvent(DebugEventType.MESSAGE_DELIVERED, "debug-mock", "Delivered message P2--1 to Group1");
        recordEvent(DebugEventType.MESSAGE_CREATED, "debug-mock", "Created message P3--1 in group Group1");
        recordEvent(DebugEventType.MESSAGE_DELIVERED, "debug-mock", "Delivered message P3--1 to Group1");
    }
}
