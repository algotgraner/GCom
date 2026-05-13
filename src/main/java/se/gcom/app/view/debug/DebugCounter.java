package se.gcom.app.view.debug;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import se.gcom.app.controller.DebugController;
import se.gcom.app.debug.DebugEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DebugCounter extends VBox {
    private final DebugController controller;
    private final ComboBox<String> groupSelector = new ComboBox<>();
    TableView<CountingStats> table = new TableView<>();

    private final ListChangeListener<DebugEvent> eventListener = change -> refresh();

    public DebugCounter(DebugController controller) {
        this.controller = controller;
        createLayout();
        setupActions();
        refresh();
    }

    private void createLayout() {
        setPadding(new Insets(16));
        setSpacing(12);

        Label title = new Label("Message Counters");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        groupSelector.setPromptText("Group");

        HBox controls = new HBox(8, groupSelector);

        Label membersLabel = new Label("Messages:");

        TableColumn<CountingStats, String> messageIDColumn = new TableColumn<>("Message");
        messageIDColumn.setCellValueFactory(new PropertyValueFactory<>("groupName"));

        TableColumn<CountingStats, Integer> countColumn = new TableColumn<>("Count");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("messageCount"));

        table.getColumns().addAll(messageIDColumn, countColumn);

        HBox tableBox = new HBox(8, table);
        getChildren().addAll(title, controls, membersLabel, tableBox);

    }

    private void setupActions() {
        groupSelector.setOnAction(event -> refreshContent());
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
        HashMap<String, Integer> map = controller.getMessageCountMap(group);
        for (String message : map.keySet()) {
            int count = map.get(message);
            table.setItems(FXCollections.observableArrayList(new CountingStats(message, count)));
        }

    }

    public static class CountingStats {

        private final String messageID;
        private final int messageCount;

        public CountingStats(String messageID, int messageCount) {
            this.messageID = messageID;
            this.messageCount = messageCount;
        }

        public String getMessageID() {
            return messageID;
        }

        public int getMessageCount() {
            return messageCount;
        }
    }
}
