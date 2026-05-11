package se.gcom.app.controller;

import javafx.collections.ObservableList;
import se.gcom.app.debug.DebugEvent;
import se.gcom.middleware.Manager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public List<String> getGroupNames() {
        if (manager == null) {
            return Collections.emptyList();
        }
        return manager.getGroupNames();
    }

    public Map<String, Integer> getCurrentVectorClock(String groupName) {
        if (manager == null || groupName == null || groupName.isBlank()) {
            return Collections.emptyMap();
        }
        return manager.getCurrentVectorClock(groupName);
    }

    public void setVectorClockValue(String groupName, String process, int value) {
        if (manager != null && groupName != null && process != null) {
            manager.setVectorClockValue(groupName, process, value);
        }
    }

    public List<String> getMembers(String groupName) {
        if (manager == null || groupName == null || groupName.isBlank()) {
            return Collections.emptyList();
        }
        return manager.getMembers(groupName);
    }

    public String getMyAddress() {
        if (manager == null) {
            return "";
        }
        return manager.getMyAddress();
    }
}
