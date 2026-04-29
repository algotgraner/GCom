package se.gcom.middleware;

import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.middleware.communicationModule.CommunicationGrpcHandler;
import se.gcom.middleware.communicationModule.CommunicationGrpcSender;

public class Manager {

    ChatController chatController;
    DebugController debugController;
    CommunicationGrpcSender communicationGrpcSender;
    CommunicationGrpcHandler communicationGrpcHandler;

    public Manager(ChatController chatController, DebugController debugController) {
        this.chatController = chatController;
        this.debugController = debugController;
    }

    public void sendMessage(String sender, String message){

    }

}
