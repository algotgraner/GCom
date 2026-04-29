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

    public void sendMessage(String text) {
        if (selectedGroup == null || text == null || text.isBlank()) {
            return;
        }

        selectedGroup.getMessages().add(new ChatMessage("Me", text, true));

        if (manager != null) {
            manager.sendMessage(selectedGroup.getId(), text);
        }
    }

    public void receiveMessage(String sender, String text) {
        ChatGroup targetGroup = selectedGroup != null ? selectedGroup : groups.get(0);
        Platform.runLater(() ->
                targetGroup.getMessages().add(new ChatMessage(sender, text, false))
        );
    }

    public void addDemo(){
        receiveMessage("Group 1", "Hej");
    }
}
