package se.gcom.middleware;

import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;

public class Manager {

    ChatController chatController;
    DebugController debugController;

    public Manager(ChatController chatController, DebugController debugController) {
        this.chatController = chatController;
        this.debugController = debugController;
    }
}
