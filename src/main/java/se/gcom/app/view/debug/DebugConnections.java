package se.gcom.app.view.debug;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.gcom.app.controller.DebugController;
import se.gcom.app.debug.DebugEvent;

import java.util.ArrayList;
import java.util.List;

public class DebugConnections extends VBox {
    private final DebugController controller;
    private final ComboBox<String> groupSelector = new ComboBox<>();
    private final TableView<String> connectedClientsTable = new TableView<>();

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

        Label connectedClientsLabel = new Label("Connected clients");
        TableColumn<String, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue()));

        TableColumn<String, Void> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(event -> removeConnection(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeButton);
            }
        });

        connectedClientsTable.getColumns().add(nameColumn);
        connectedClientsTable.getColumns().add(actionColumn);
        connectedClientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        connectedClientsTable.setPlaceholder(new Label("No connected clients"));
        VBox.setVgrow(connectedClientsTable, Priority.ALWAYS);

        getChildren().addAll(title, controls, connectedClientsLabel, connectedClientsTable);

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
        List<String> members = new ArrayList<>(controller.getGroupMembers(group));
        members.sort(String::compareToIgnoreCase);
        connectedClientsTable.setItems(FXCollections.observableArrayList(members));
    }

    private void removeConnection(String address) {
        String group = groupSelector.getValue();

        if (address == null || group == null) {
            return;
        }

        controller.removeMember(group, address);
        connectedClientsTable.getItems().remove(address);
    }
}
