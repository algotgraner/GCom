package se.gcom.middleware;

import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.middleware.communicationModule.ChatMessage;

public class Manager {

    ChatController chatController;
    DebugController debugController;

    public Manager(ChatController chatController, DebugController debugController) {
        this.chatController = chatController;
        this.debugController = debugController;
    }

    public void send(ChatMessage msg){
        // 1. call group manager to get what IPs to send to and metadata about the group setup

        // 2. call ordering module to fix the ordering

        // 3. call communication module to send the actual message
    }

    public void showMessage(ChatMessage msg){
        // this function is called so that the GUI can be updated with the new text
    }
}
