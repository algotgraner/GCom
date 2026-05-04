package se.gcom.middleware;

import io.grpc.StatusRuntimeException;
import se.NameServer.NamingServer;
import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.app.model.ChatGroup;
import se.gcom.middleware.communicationModule.ChatMessage;
import se.gcom.middleware.communicationModule.CommunicationService;
import se.gcom.middleware.communicationModule.GroupMembership;
import se.gcom.middleware.communicationModule.Message;
import se.gcom.middleware.groupManagementModule.GroupManagement;

import java.util.ArrayList;
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

    public void start() {
        communicationService = new CommunicationService(this);
        int port = startServer();
        this.groupManagement = new GroupManagement(port);
    }

    public void sendMessage(String groupName, String message) {
        List<String> addresses = groupManagement.getAddresses(groupName);
        // Create the message
        ChatMessage msg = ChatMessage.newBuilder()
                .setMessageId("1")
                .setSenderId(groupManagement.getAddress())
                .setReceiverId("Test")
                .setPayload(message)
                .build();

        Message m = Message.newBuilder().setChatMessage(msg).build();

        System.out.println("Sent to:" + addresses);

        communicationService.multicast(m, addresses);
    }

    public void receiveMessage(ChatMessage msg) {
        boolean outgoing = msg.getSenderId().equals(groupManagement.getAddress());
        chatController.receiveMessage(msg.getSenderId(), msg.getPayload(), outgoing);
    }

    public void receiveMessage(GroupMembership msg) {
        if (msg.getJoining()) {
            groupManagement.addNewMember(msg.getGroupId(), msg.getSenderId());
        } else {
            groupManagement.removeMember(msg.getGroupId(), msg.getSenderId());
        }
    }

    public void addUserToGroup(String groupName, String ipAddress, String name, int port) {
        System.out.println("groupName: " + groupName);
        System.out.println("ipAddress: " + ipAddress);
        System.out.println("name: " + name);
        System.out.println("port: " + port);

    }

    public void addGroup(String groupName, String ipAddress, String name, int port) {
        groupManagement.createNewGroup(groupName, new ArrayList<>());
    }

    private int startServer() {
        return communicationService.start();
    }

    public void joinGroup(String name) throws StatusRuntimeException {

        ArrayList<String> addresses = groupManagement.joinGroup(name);
        GroupMembership g = GroupMembership.newBuilder()
                .setGroupId(name)
                .setSenderId(groupManagement.getAddress())
                .setJoining(true)
                .setMessageId("1")
                .build();
        Message m = Message.newBuilder().setGroupMembership(g).build();
        communicationService.multicast(m, addresses);
    }

    public void leaveGroup(ChatGroup group) {
        groupManagement.leaveGroup(group.getId());
        GroupMembership g = GroupMembership.newBuilder()
                .setGroupId(group.getId())
                .setSenderId(groupManagement.getAddress())
                .setJoining(false)
                .setMessageId("1")
                .build();
        Message m = Message.newBuilder().setGroupMembership(g).build();
        ArrayList<String> addresses = new ArrayList<>(groupManagement.getAddresses(group.getName()));
        addresses.remove(groupManagement.getAddress());
        communicationService.multicast(m, addresses);
    }
}
