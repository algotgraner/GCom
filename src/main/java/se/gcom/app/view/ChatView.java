package se.gcom.app.view;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import se.gcom.app.controller.ChatController;
import se.gcom.app.model.ChatGroup;
import se.gcom.app.model.ChatMessage;


public class ChatView {
    private final BorderPane root;
    private final ChatController chatController;
    private final ListView<ChatGroup> groups = new ListView<>();
    private final VBox messagesBox = new VBox(10);
    private final TextField inputField = new TextField();
    private final Button sendButton = new Button("Send");
    private ObservableList<ChatMessage> visibleMessages;
    private final ListChangeListener<ChatMessage> messageListener = change -> renderMessages();

    public ChatView(ChatController chatController) {
        this.chatController = chatController;
        this.root = new BorderPane();
        createLayout();
        setupActions();
    }

    private void createLayout() {
        VBox leftPane = createLeftPane();
        VBox rightPane = createRightPane();

        root.setTop(createMenuBar());
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

        groups.setItems(chatController.getGroups());
        groups.getSelectionModel().selectedItemProperty().addListener((obs, oldGroup, newGroup) -> {
            chatController.selectGroup(newGroup);
            showMessages(newGroup);
        });

        groups.getSelectionModel().selectFirst();
        showMessages(groups.getSelectionModel().getSelectedItem());

        VBox leftPane = new VBox(10, title, groups);
        leftPane.setPadding(new Insets(0, 12, 0, 0));
        leftPane.setPrefWidth(260);
        VBox.setVgrow(groups, Priority.ALWAYS);

        return leftPane;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        Menu windowMenu = new Menu("Debug");

        MenuItem debugItem = new MenuItem("Open Debug Window");
        debugItem.setOnAction(e -> openDebugWindow());

        debugItem.setAccelerator(
                new KeyCodeCombination(
                        KeyCode.D,
                        KeyCombination.SHORTCUT_DOWN,
                        KeyCombination.SHIFT_DOWN
                )
        );

        windowMenu.getItems().add(debugItem);
        menuBar.getMenus().add(windowMenu);

        return menuBar;
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

        chatController.sendMessage(text);
        inputField.clear();
    }

    private void showMessages(ChatGroup group) {
        if (visibleMessages != null) {
            visibleMessages.removeListener(messageListener);
        }

        visibleMessages = group == null ? null : group.getMessages();
        if (visibleMessages != null) {
            visibleMessages.addListener(messageListener);
        }
        renderMessages();
    }

    private void renderMessages() {
        messagesBox.getChildren().clear();
        if (visibleMessages == null) {
            return;
        }

        for (ChatMessage message : visibleMessages) {
            messagesBox.getChildren().add(createMessageRow(message));
        }
    }

    private HBox createMessageRow(ChatMessage message) {
        if (message.isOutgoing()) {
            return createOutgoingMessage(message.getText());
        }
        return createIncomingMessage(message.getSender(), message.getText());
    }

    private HBox createIncomingMessage(String sender, String text) {
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

        return row;
    }

    private HBox createOutgoingMessage(String text) {
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

        return row;
    }

    private void openDebugWindow() {
        DebugView debugView = new DebugView();

        Stage debugStage = new Stage();
        debugStage.setTitle("Debug View");
        debugStage.setScene(new Scene(debugView, 1600, 1000));
        debugStage.show();
    }


    public Parent getRoot() {
        return root;
    }
}
