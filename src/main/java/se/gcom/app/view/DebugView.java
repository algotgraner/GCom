package se.gcom.app.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se.gcom.app.view.debug.DebugOverview;

public class DebugView extends BorderPane {

    private final ListView<String> sidebar = new ListView<>();

    public DebugView(){
        createLayout();
       // setupActions();

    }

    private void createLayout() {
        sidebar.setItems(FXCollections.observableArrayList(
                "Overview",
                "Network",
                "Messages",
                "Settings"
        ));

        sidebar.setPrefWidth(180);
        sidebar.getSelectionModel().selectFirst();

        VBox leftPane = new VBox(sidebar);
        leftPane.setPadding(new Insets(0,0,0,0));
        leftPane.setPrefWidth(200);
        VBox.setVgrow(sidebar, Priority.ALWAYS);

        setLeft(leftPane);
        setCenter(new DebugOverview());
    }

}
