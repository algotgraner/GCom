package se.gcom.app.view.debug;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import se.gcom.app.controller.DebugController;
import se.gcom.app.debug.DebugEvent;

import java.util.ArrayList;
import java.util.List;

public class DebugPaths extends VBox {
    private final DebugController controller;
    private final ComboBox<String> groupSelector = new ComboBox<>();
    private final ComboBox<String> memberSelector = new ComboBox<>();
    Pane canvas = new Pane();

    private final ListChangeListener<DebugEvent> eventListener = change -> refresh();

    public DebugPaths(DebugController controller) {
        this.controller = controller;
        createLayout();
        setupActions();
        refresh();
    }

    private void createLayout() {
        setPadding(new Insets(16));
        setSpacing(12);

        Label title = new Label("Paths");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        groupSelector.setPromptText("Group");

        HBox controls = new HBox(8, groupSelector);

        Label membersLabel = new Label("Messages:");

        memberSelector.setPromptText("Message");

        HBox members = new HBox(8, memberSelector);

        HBox paths = new HBox(8, canvas);

        getChildren().addAll(title, controls, membersLabel, members, paths);

    }

    private void setupActions() {
        groupSelector.setOnAction(event -> refreshContent());
        memberSelector.setOnAction(event -> displayPaths());
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

        List<String> messages = controller.getMessages(group);

        if (messages == null) {
            messages = new ArrayList<>();
        }
        memberSelector.setItems(FXCollections.observableArrayList(messages));
    }

    private void displayPaths(){
        String message = memberSelector.getValue();
        ArrayList<ArrayList<String>> paths = controller.getPaths(message);
        if (paths == null || paths.isEmpty()) {
            return;
        }
        canvas.getChildren().clear();
        int i = 1;
        for (ArrayList<String> path : paths) {
            renderPath(path, i);
            i++;
            System.out.println("Path " + i + " " + path);
        }
    }
    private void renderPath(List<String> path, int i) {

        double x = 50 * i;
        double y = 50;

        Circle prev = null;

        for (String node : path) {

            Circle c = new Circle(x, y, 15);
            c.setFill(javafx.scene.paint.Color.CORNFLOWERBLUE);
            c.setStroke(Color.WHITE);
            c.setStrokeWidth(2);
            Label label = new Label(node);

            label.setLayoutX(x - 10);
            label.setLayoutY(y - 10);

            canvas.getChildren().addAll(c, label);

            if (prev != null) {
                Line line = new Line(
                        prev.getCenterX(), prev.getCenterY(),
                        x, y
                );
                line.setStroke(Color.WHITE);
                line.setStrokeWidth(2);
                canvas.getChildren().add(line);
            }


            prev = c;
            y += 70; // vertical spacing
        }
    }
}
