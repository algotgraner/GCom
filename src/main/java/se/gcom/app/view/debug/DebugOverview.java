package se.gcom.app.view.debug;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import se.gcom.app.view.DebugView;

public class DebugOverview extends VBox {

    public DebugOverview() {
        setPadding(new Insets(16));
        setSpacing(12);

        getChildren().add(new Label("Debug Overview"));

    }
}
