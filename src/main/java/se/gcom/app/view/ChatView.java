package se.gcom.app.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import se.gcom.app.controller.ChatController;


public class ChatView {
    private final BorderPane root;
    private final ChatController chatController;
    private final ListView<String> groups = new ListView<>();
    private final VBox messagesBox = new VBox(10);
    private final TextField inputField = new TextField();
    private final Button sendButton = new Button("Send");

    public ChatView(ChatController chatController) {
        this.root = new BorderPane();
        createLayout();
        setupActions();
        this.chatController = chatController;
    }

    private void createLayout() {
        VBox leftPane = createLeftPane();
        VBox rightPane = createRightPane();

        root.setLeft(leftPane);
        root.setCenter(rightPane);
    }

    private VBox createRightPane() {
        messagesBox.setPadding(new Insets(10));
        messagesBox.setFillWidth(true);

        ScrollPane scrollPane = new ScrollPane(messagesBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        inputField.setPromptText("Write a message...");
        HBox.setHgrow(inputField, Priority.ALWAYS);

        HBox inputBar = new HBox(10, inputField, sendButton);
        inputBar.setPadding(new Insets(8,25,8,12));

        VBox rightPane = new VBox(10,
                scrollPane,
                inputBar
        );
        rightPane.setPadding(new Insets(0, 0, 0, 12));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return rightPane;

    }

    private VBox createLeftPane() {
        Label title = new Label("Groups");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        groups.setItems(FXCollections.observableArrayList(
                "Group 1",
                "Group 2"
        ));

        groups.getSelectionModel().selectFirst();

        VBox leftPane = new VBox(10, title, groups);
        leftPane.setPadding(new Insets(0, 12, 0, 0));
        leftPane.setPrefWidth(260);
        VBox.setVgrow(groups, Priority.ALWAYS);

        return leftPane;
    }

    private void setupActions() {
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        addOutgoingMessage(text);
        chatController.sendMessage("Group 1", text);
        inputField.clear();
    }

    public void addIncomingMessage(String sender, String text) {
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 11px; -fx-opacity: 0.8;");

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(10));
        bubble.setStyle(
                "-fx-background-color: -color-neutral-muted;" +
                        "-fx-background-radius: 12;"
        );

        VBox bubbleBox = new VBox(4, senderLabel, bubble);
        bubbleBox.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(bubbleBox);
        row.setAlignment(Pos.CENTER_LEFT);

        messagesBox.getChildren().add(row);
    }

    private void addOutgoingMessage(String text) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(400);
        bubble.setPadding(new Insets(10));
        bubble.setStyle(
                "-fx-background-color: -color-accent-emphasis;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 12;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(spacer, bubble);
        row.setAlignment(Pos.CENTER_RIGHT);

        messagesBox.getChildren().add(row);
    }


    public Parent getRoot() {
        return root;
    }
}