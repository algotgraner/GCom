package se.gcom.app.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import se.gcom.app.model.ChatGroup;
import se.gcom.app.model.ChatMessage;
import se.gcom.middleware.Manager;

public class ChatController {

    private Manager manager;

    private final ObservableList<ChatGroup> groups =
            FXCollections.observableArrayList();

    private ChatGroup selectedGroup;

    public ChatController() {
        groups.add(new ChatGroup("Group1", "Group 1"));
        groups.add(new ChatGroup("Group2", "Group 2"));
        selectedGroup = groups.get(0);
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public ObservableList<ChatGroup> getGroups() {
        return groups;
    }

    public ChatGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void selectGroup(ChatGroup group) {
        if (group != null) {
            selectedGroup = group;
        }
    }

    public ChatGroup createGroup(String name, String ipAddress, String username, int port) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String trimmedName = name.trim();
        ChatGroup group = new ChatGroup(createGroupId(trimmedName), trimmedName);
        groups.add(group);
        selectedGroup = group;

        if (manager != null) {
            manager.addGroup(trimmedName, ipAddress, username, port);
        }

        return group;
    }

    public void sendMessage(String text) {
        if (selectedGroup == null || text == null || text.isBlank()) {
            return;
        }

        if (manager != null) {
            manager.sendMessage(selectedGroup.getId(), text);
        }
    }

    public void addUserToSelectedGroup(String ipAddress, String name, int port) {
        if (selectedGroup == null || manager == null) {
            return;
        }

        manager.addUserToGroup(selectedGroup.getId(), ipAddress, name, port);
    }

    public void receiveMessage(String sender, String text, boolean outgoing) {
        ChatGroup targetGroup = selectedGroup != null ? selectedGroup : groups.get(0);
        System.out.println("Outgoing: " + outgoing);
        Platform.runLater(() ->
                targetGroup.getMessages().add(new ChatMessage(sender, text, outgoing))
        );
    }


    private String createGroupId(String name) {
        String baseId = name.replaceAll("\\s+", "");
        if (baseId.isEmpty()) {
            baseId = "Group";
        }

        String candidate = baseId;
        int suffix = 1;
        while (groupIdExists(candidate)) {
            candidate = baseId + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean groupIdExists(String id) {
        for (ChatGroup group : groups) {
            if (group.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public ChatGroup joinGroup(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        String trimmedName = name.trim();
        ChatGroup existingGroup = findGroup(trimmedName);
        if (existingGroup != null) {
            selectedGroup = existingGroup;
            return existingGroup;
        }

        ChatGroup group = new ChatGroup(trimmedName, trimmedName);
        groups.add(group);
        selectedGroup = group;

        if (manager != null) {
            manager.joinGroup(trimmedName);
        }

        return group;
    }

    private ChatGroup findGroup(String name) {
        for (ChatGroup group : groups) {
            if (group.getId().equals(name) || group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }

    public void leaveGroup() {
        ChatGroup group = selectedGroup;
        manager.leaveGroup(group);
        groups.remove(group);
    }
}
