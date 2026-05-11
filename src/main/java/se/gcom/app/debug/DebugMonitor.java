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
        recordEvent(DebugEventType.VECTOR_CLOCK_UPDATED, "debug-mock", "group=Group1 action=send vc={A=2, B=1, C=0}");
        recordEvent(DebugEventType.MESSAGE_HELD_BACK, "debug-mock", "group=Group1 message=P3--1 sender=C incoming={A=2, B=1, C=2}");
    }
}
