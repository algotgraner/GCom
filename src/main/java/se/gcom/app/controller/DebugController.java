package se.gcom.app.controller;

import javafx.collections.ObservableList;
import se.gcom.app.debug.DebugEvent;
import se.gcom.middleware.Manager;
import se.gcom.middleware.communicationModule.ChatMessage;

import java.util.ArrayList;
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

    public List<String> getGroupMembers(String groupName) {
        return manager.getGroupMembers(groupName);
    }

    public void removeMember(String groupName, String address) {
        manager.removeMember(groupName, address);
    }

    public void setMemberEnabled(String groupName, String address, boolean enabled) {
        if (manager == null || groupName == null || address == null) {
            return;
        }

        if (enabled) {
            manager.addMember(groupName, address);
        } else {
            manager.removeMember(groupName, address);
        }
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

    public List<ChatMessage> getHoldbackQueue(String groupName) {
        if (manager == null || groupName == null || groupName.isBlank()) {
            return Collections.emptyList();
        }
        return manager.getHoldbackQueue(groupName);
    }
    public ArrayList<String> getMessages(String groupName) {
        return manager.getMessages(groupName);
    }
    public ArrayList<ArrayList<String>> getPaths(String message){
        return manager.getPaths(message);
    }

    public String getMyAddress() {
        if (manager == null) {
            return "";
        }
        return manager.getMyAddress();
    }
}
