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
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Joined group Group1");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Vector clock initialized {P1=1, P2=0, P3=0}");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Created message P1--1");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Sent message P1--1 to P2");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Sent message P1--1 to P3");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Received ACK from P2 for P1--1");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Received ACK from P3 for P1--1");
        recordEvent(DebugEventType.PROCESS_STARTED, "debug-mock", "Delivered message P1--1");
    }
}
