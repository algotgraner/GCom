package se.gcom;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.app.view.ChatView;
import se.gcom.middleware.Manager;

public class App extends Application {

    @Override
    public void start(Stage Stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        ChatController chatController = new ChatController();
        DebugController debugController = new DebugController();
        Manager manager = new Manager(chatController, debugController);

        chatController.setManager(manager);
        debugController.setManager(manager);

        ChatView chatView = new ChatView(chatController);

        manager.start(5000);

        Stage.setTitle("GCom Chat");
        Stage.setScene(new Scene(chatView.getRoot(), 900, 600));
        Stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
