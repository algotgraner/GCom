package se.gcom.app.controller;

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

        receiveMessage("Group 1", result);
    }

    public void receiveMessage(String sender, String text){
        chatView.addIncomingMessage(sender, text);
    }

    public void addDemo(){
        receiveMessage("Group 1", "Hej");
    }
}
