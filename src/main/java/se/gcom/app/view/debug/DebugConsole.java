package se.gcom.app.view.debug;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.gcom.app.controller.DebugController;
import se.gcom.app.debug.DebugEvent;

public class DebugConsole extends VBox {

    public DebugConsole(DebugController controller) {
        setPadding(new Insets(16));
        setSpacing(12);

        Button mockEventsButton = new Button("Add mock events");
        mockEventsButton.setOnAction(event -> controller.addMockEvents());

        ListView<DebugEvent> listView = new ListView<>();
        listView.setItems(controller.getEvents());

        listView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(DebugEvent event, boolean empty) {
                super.updateItem(event, empty);

                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.toDisplayString());
                }
            }
        });

        VBox.setVgrow(listView, Priority.ALWAYS);
        getChildren().addAll(mockEventsButton, listView);
    }
}
