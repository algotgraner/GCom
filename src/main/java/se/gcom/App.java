package se.gcom;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.gcom.app.controller.ChatController;
import se.gcom.app.view.ChatView;

public class App extends Application {

    @Override
    public void start(Stage Stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        ChatController chatController = new ChatController();
        ChatView chatView = new ChatView();

        Stage.setTitle("GCom Chat");
        Stage.setScene(new Scene(chatView.getRoot(), 900, 600));
        Stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}