package se.gcom.middleware;

import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.middleware.communicationModule.ChatMessage;
import se.gcom.middleware.communicationModule.CommunicationGrpcHandler;
import se.gcom.middleware.communicationModule.CommunicationGrpcSender;
import se.gcom.middleware.groupManagementModule.GroupManagement;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.List;

public class Manager {

    ChatController chatController;
    DebugController debugController;

    GroupManagement groupManagement;
    CommunicationGrpcSender communicationGrpcSender;
    CommunicationGrpcHandler communicationGrpcHandler;

    private Server server;

    public Manager(ChatController chatController, DebugController debugController) {
        this.chatController = chatController;
        this.debugController = debugController;
    }

    public void start(int port){
        this.groupManagement = new GroupManagement(port);
        this.communicationGrpcSender = new CommunicationGrpcSender();
        this.communicationGrpcHandler = new CommunicationGrpcHandler(this);

        startServer(port);
    }

    public void sendMessage(String groupName, String message){
        List<String> addresses = groupManagement.getAddresses(groupName);
        // Create the message
        ChatMessage msg = ChatMessage.newBuilder()
                        .setMessageId("1")
                        .setSenderId("Jonis")
                        .setReceiverId("Test")
                        .setPayload(message)
                        .build();
        communicationGrpcSender.multicast(msg, addresses);
    }

    public void receiveMessage(ChatMessage msg){
        chatController.receiveMessage(msg.getSenderId(), msg.getPayload());
    }

    private void startServer(int port) {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(communicationGrpcHandler)
                    .build()
                    .start();
            System.out.println("Listening on port: " + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
