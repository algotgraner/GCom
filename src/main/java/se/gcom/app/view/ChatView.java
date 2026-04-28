package se.gcom.app.view;

import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class ChatView {
    private final BorderPane root;

    public ChatView() {
        this.root = new BorderPane();
    }

    public Parent getRoot() {
        return root;
    }
}