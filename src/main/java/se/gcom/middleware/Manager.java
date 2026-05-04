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
import se.gcom.middleware.messageOrderingModule.OrderingModule;

import java.util.ArrayList;
import java.util.List;

public class Manager {

    ChatController chatController;
    DebugController debugController;
    OrderingModule orderingModule;
    String myAddress;
    private long messageCounter;

    GroupManagement groupManagement;
    CommunicationService communicationService;

    public Manager(ChatController chatController, DebugController debugController) {
        this.chatController = chatController;
        this.debugController = debugController;
    }

    public void start(){
        communicationService = new CommunicationService(this);
        int port = startServer();
        this.groupManagement = new GroupManagement(port);
        this.myAddress = groupManagement.getAddress();
        this.orderingModule = new OrderingModule(this, myAddress);
    }

    private String generateMessageId() {
        return groupManagement.getAddress() + "--" + (++messageCounter);
    }

    public void sendMessage(String groupName, String message){
        List<String> addresses = groupManagement.getAddresses(groupName);
        // Create the message
        ChatMessage msg = ChatMessage.newBuilder()
                        .setMessageId(generateMessageId())
                        .setSenderId(groupManagement.getAddress())
                        .setGroupId(groupName)
                        .setPayload(message)
                        .build();

        // let ordering module append the vector clock if needed
        ChatMessage finalMsg = orderingModule.handleOutgoingMessage(msg, msg.getGroupId());
        Message m = Message.newBuilder().setChatMessage(finalMsg).build();
        // print all messages for debug
        System.out.println(addresses);
        // let communication module send the message
        communicationService.multicast(m, addresses);
    }

    public void handleIncomingMessage(ChatMessage msg){
        orderingModule.handleIncomingMessage(msg);
    }

    public void deliverIncomingMessage(ChatMessage msg){
        boolean outgoing = msg.getSenderId().equals(groupManagement.getAddress());
        chatController.receiveMessage(msg.getSenderId(), msg.getPayload(), outgoing);
    }

    public void deliverIncomingMessage(GroupMembership msg) {
        if (msg.getJoining()) {
            groupManagement.addNewMember(msg.getGroupId(), msg.getSenderId());
            System.out.println("Received join message");
        } else {
            groupManagement.removeMember(msg.getGroupId(), msg.getSenderId());
            System.out.println("Received remove message");
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
        GroupMembership g = GroupMembership.newBuilder()
                .setGroupId(group.getId())
                .setSenderId(groupManagement.getAddress())
                .setJoining(false)
                .setMessageId("1")
                .build();
        Message m = Message.newBuilder().setGroupMembership(g).build();
        ArrayList<String> addresses = new ArrayList<>(groupManagement.getAddresses(group.getName()));
        groupManagement.leaveGroup(group.getId());
        addresses.remove(groupManagement.getAddress());
        System.out.println("Sending Leave to:" + addresses);
        communicationService.multicast(m, addresses);
    }

}
