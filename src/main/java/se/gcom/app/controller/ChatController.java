package se.gcom.app.controller;

import javafx.application.Platform;
import se.gcom.app.view.ChatView;
import se.gcom.middleware.Manager;

public class ChatController {

    ChatView chatView;
    Manager manager;

    public void setChatView(ChatView chatView){
        this.chatView = chatView;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public void sendMessage(String sender, String text) {
        String result = "Text sent: " + text;

        manager.sendMessage("Group1", text);
    }

    public void receiveMessage(String sender, String text){
        Platform.runLater(() ->
                chatView.addIncomingMessage(sender, text)
                );
    }

    public void addDemo(){
        receiveMessage("Group 1", "Hej");
    }
}
