package se.gcom.middleware;

import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.middleware.communicationModule.ChatMessage;
import se.gcom.middleware.communicationModule.CommunicationService;
import se.gcom.middleware.groupManagementModule.GroupManagement;

import java.util.List;

public class Manager {

    ChatController chatController;
    DebugController debugController;

    GroupManagement groupManagement;
    CommunicationService communicationService;

    public Manager(ChatController chatController, DebugController debugController) {
        this.chatController = chatController;
        this.debugController = debugController;
    }

    public void start(int port){
        this.groupManagement = new GroupManagement(port);
        communicationService = new CommunicationService(this);
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
        communicationService.multicast(msg, addresses);
    }

    public void receiveMessage(ChatMessage msg){
        chatController.receiveMessage(msg.getSenderId(), msg.getPayload());
    }

    public void addUserToGroup(String groupName, String ipAddress, String name, int port) {
        System.out.println("groupName: " + groupName);
        System.out.println("ipAddress: " + ipAddress);
        System.out.println("name: " + name);
        System.out.println("port: " + port);

    }

    public void addGroup(String groupName, String ipAddress, String name, int port) {
        System.out.println("groupName: " + groupName);
        System.out.println("ipAddress: " + ipAddress);
        System.out.println("name: " + name);
        System.out.println("port: " + port);
    }

    private void startServer(int port) {
        communicationService.start(port);
    }

}
