package se.gcom.app.view.debug;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import se.gcom.app.controller.DebugController;
import se.gcom.app.debug.DebugEvent;

import java.util.ArrayList;
import java.util.List;

public class DebugConnections extends VBox {
    private final DebugController controller;
    private final ComboBox<String> groupSelector = new ComboBox<>();
    private final ComboBox<String> memberSelector = new ComboBox<>();
    private final ListChangeListener<DebugEvent> eventListener = change -> refresh();

    public DebugConnections(DebugController controller) {
        this.controller = controller;
        createLayout();
        setupActions();
        refresh();
    }

    private void createLayout() {
        setPadding(new Insets(16));
        setSpacing(12);

        Label title = new Label("Connections");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        groupSelector.setPromptText("Group");

        HBox controls = new HBox(8, groupSelector);

        Label membersLabel = new Label("Remove Connection:");

        memberSelector.setPromptText("Member");

        HBox members = new HBox(8, memberSelector);

        getChildren().addAll(title, controls, membersLabel, members);

    }

    private void setupActions() {
        groupSelector.setOnAction(event -> refreshContent());
        memberSelector.setOnAction(event -> removeConnection());
        controller.getEvents().addListener(eventListener);
    }

    private void refresh() {
        String selectedGroup = groupSelector.getValue();
        List<String> groups = new ArrayList<>(controller.getGroupNames());
        groups.sort(String::compareToIgnoreCase);
        groupSelector.setItems(FXCollections.observableArrayList(groups));

        if (selectedGroup != null && groups.contains(selectedGroup)) {
            groupSelector.setValue(selectedGroup);
        } else if (!groups.isEmpty()) {
            groupSelector.setValue(groups.getFirst());
        }

        refreshContent();
    }

    private void refreshContent() {
        String group = groupSelector.getValue();
        List<String> members = new ArrayList<>(controller.getGroupMembers(group));
        memberSelector.setItems(FXCollections.observableArrayList(members));
    }

    private void removeConnection() {
        String address =  memberSelector.getValue();
        String group = groupSelector.getValue();
        controller.removeMember(group, address);
        List<String> members = new ArrayList<>(controller.getGroupMembers(group));
        memberSelector.setItems(FXCollections.observableArrayList(members));
    }
}
