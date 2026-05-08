package se.gcom.app.controller;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import se.gcom.app.debug.DebugEvent;
import se.gcom.app.debug.DebugMonitor;
import se.gcom.middleware.Manager;

public class DebugController {
    private Manager manager;

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public ObservableList<DebugEvent> getEvents(){
        return manager.getDebugMonitor().getEvents();
    }

    public void addMockEvents() {
        manager.getDebugMonitor().addMockEvents();
    }
}
