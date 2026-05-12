package se.gcom.middleware;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import se.gcom.app.controller.ChatController;
import se.gcom.app.controller.DebugController;
import se.gcom.app.debug.DebugEventType;
import se.gcom.app.debug.DebugMonitor;
import se.gcom.app.model.ChatGroup;
import se.gcom.middleware.communicationModule.*;
import se.gcom.middleware.groupManagementModule.GroupManagement;
import se.gcom.middleware.messageOrderingModule.OrderingModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Manager {

    ChatController chatController;
    DebugController debugController;
    OrderingModule orderingModule;
    String myAddress;
    private long messageCounter;
    private final DebugMonitor debugMonitor = new DebugMonitor();

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
        debugMonitor.recordEvent(
                DebugEventType.PROCESS_STARTED,
                myAddress,
                "Communication service started on port " + port
        );
    }

    public String getMyAddress() {
        return myAddress;
    }

    private String generateMessageId() {
        return groupManagement.getAddress() + "--" + (++messageCounter);
    }

    public void sendMessage(String groupName, String message){
        List<String> addresses = groupManagement.getAddresses(groupName);
        // Create the message

        String messageId = generateMessageId();
        ChatMessage msg = ChatMessage.newBuilder()
                        .setMessageId(messageId)
                        .setSenderId(groupManagement.getAddress())
                        .setGroupId(groupName)
                        .setPayload(message)
                        .build();

        if(!groupManagement.isStaticGroup(groupName) || groupManagement.canSendMessages(groupName)){
            debugMonitor.recordEvent(
                    DebugEventType.MESSAGE_CREATED,
                    myAddress,
                    "Created message " + messageId + " in group " + groupName + ": " + message
            );
            // let ordering module append the vector clock if needed
            ChatMessage finalMsg = orderingModule.handleOutgoingMessage(msg, msg.getGroupId());
            Message m = Message.newBuilder().setChatMessage(finalMsg).build();
            // print all messages for debug
            System.out.println("Sending to" + addresses);
            // let communication module send the message
            communicationService.multicast(m, addresses);
        }
    }

    public void handleIncomingMessage(ChatMessage msg, boolean reliable){
        orderingModule.handleIncomingMessage(msg);
        if (reliable) {
            Message m = Message.newBuilder().setChatMessage(msg).build();
            List<String> addresses = groupManagement.getAddresses(msg.getGroupId());
            addresses.remove(msg.getSenderId());
            communicationService.multicast(m, addresses);
        }
    }

    public void deliverIncomingMessage(ChatMessage msg){
        boolean outgoing = msg.getSenderId().equals(groupManagement.getAddress());
        chatController.receiveMessage(msg.getSenderId(), msg.getGroupId(), msg.getPayload(), outgoing);
        debugMonitor.recordEvent(
                DebugEventType.MESSAGE_DELIVERED,
                myAddress,
                "Delivered message " + msg.getMessageId()
                        + " from " + msg.getSenderId()
                        + " in group " + msg.getGroupId()
                        + ": " + msg.getPayload()
        );
    }

    public void deliverIncomingMessage(GroupMembership msg) {
        if (msg.getJoining()) {
            if(!groupManagement.isStaticGroup(msg.getGroupId()) || groupManagement.canJoinStaticGroup(msg.getGroupId(), msg.getSenderId())) {
                groupManagement.addNewMember(msg.getGroupId(), msg.getSenderId());
                orderingModule.addMemberToVectorClock(msg.getGroupId(), msg.getSenderId());
                System.out.println("Received join message");
            }
        } else {
            groupManagement.removeMember(msg.getGroupId(), msg.getSenderId());
            System.out.println("Received leave message");
        }
    }

    public void addUserToGroup(String groupName, String ipAddress, String name, int port) {
        System.out.println("groupName: " + groupName);
        System.out.println("ipAddress: " + ipAddress);
        System.out.println("name: " + name);
        System.out.println("port: " + port);

    }

    public void addGroup(String groupName) {
        groupManagement.createNewGroup(groupName, new ArrayList<>());
    }

    public void addStaticGroup(String groupName, ArrayList<String> groupMembers) {
        groupManagement.createNewStaticGroup(groupName, groupMembers);
    }
    public void addGroupToReliablePairing(String groupName, boolean reliable){
        communicationService.addGroupToReliablePairing(groupName, reliable);
    }

    private int startServer() {
        return communicationService.start();
    }

    public void joinGroup(String name, String ip) throws StatusRuntimeException {
        ArrayList<String> addresses;
        if (groupManagement.NamingServerIsUp()){
            addresses = groupManagement.joinGroup(name);
        } else {
            addresses = new ArrayList<>();
            addresses.add(ip);
        }
        GroupMembership g = GroupMembership.newBuilder()
                .setGroupId(name)
                .setSenderId(groupManagement.getAddress())
                .setJoining(true)
                .setMessageId("1")
                .build();
        Message m = Message.newBuilder().setGroupMembership(g).build();
        // Send join request
        System.out.println("Sending join request to first in member list: " + addresses.getFirst());
        Ack ack = communicationService.multicast(m, addresses);

        if (ack.getSuccess() && ack.hasMembership()){
            MembershipAck membershipAck = ack.getMembership();
            System.out.println("Got membership ack with VC: " + membershipAck.getVectorClockMap());
            System.out.println("Can join group? " + membershipAck.getCanJoinStaticGroup());
            if(!membershipAck.getIsStatic() || membershipAck.getCanJoinStaticGroup()) {;
                OrderingModule.OrderingType groupType;
                if (membershipAck.getIsCausal()) {
                    groupType = OrderingModule.OrderingType.CAUSAL;
                } else {
                    groupType = OrderingModule.OrderingType.UNORDERED;
                }
                orderingModule.setUpGroup(name, groupType);
                // join the group with the vector clock we
                orderingModule.joinGroup(name, membershipAck.getVectorClockMap());
                if (!namingServerIsUp()) {
                    ArrayList<String> newAddresses = new ArrayList<>(membershipAck.getMembersList());
                    groupManagement.joinGroup(name, newAddresses);
                    newAddresses.remove(ip);
                    newAddresses.remove(myAddress);
                    communicationService.multicast(m, newAddresses);
                }
                if (membershipAck.getIsStatic()) {
                    if (membershipAck.getCanJoinStaticGroup()) {
                        groupManagement.addStaticGroup(name, new ArrayList<>(membershipAck.getStaticMembersList()));
                        if (groupManagement.canStartSendingMessages(name)) {
                            groupManagement.addCanSendMessages(name);
                        }
                    }
                }
                communicationService.addGroupToReliablePairing(name, membershipAck.getIsReliable());
            } else {
                groupManagement.leaveGroup(name);
                throw new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("You are not allowed to join this group"));
            }
        }
    }

    public boolean namingServerIsUp(){
        return groupManagement.NamingServerIsUp();
    }
    public List<String> getMembers(String group){
        return groupManagement.getAddresses(group);
    }

    public List<String> getGroupNames() {
        return groupManagement.getGroupNames();
    }
    public List<String> getGroupMembers(String group){
        return groupManagement.getAddresses(group);
    }

    public void leaveGroup(String groupName) {
        GroupMembership g = GroupMembership.newBuilder()
                .setGroupId(groupName)
                .setSenderId(groupManagement.getAddress())
                .setJoining(false)
                .setMessageId("1")
                .build();
        Message m = Message.newBuilder().setGroupMembership(g).build();
        ArrayList<String> addresses = new ArrayList<>(groupManagement.getAddresses(groupName));
        groupManagement.leaveGroup(groupName);
        addresses.remove(groupManagement.getAddress());
        System.out.println("Sending Leave to:" + addresses);
        communicationService.multicast(m, addresses);
    }

    public Map<String, Integer> getCurrentVectorClock(String groupName) {
        return orderingModule.getVectorClock(groupName);
    }

    public void setVectorClockValue(String groupName, String process, int value) {
        orderingModule.setVectorClockValue(groupName, process, value);
    }

    public Boolean orderingIsCausal(String groupName){
        return orderingModule.orderingIsCausal(groupName);
    }

    public void setGroupOrdering(String groupId, OrderingModule.OrderingType type) {
        orderingModule.setUpGroup(groupId, type);
    }
    public boolean isStaticGroup(String groupName) {
        return groupManagement.isStaticGroup(groupName);
    }

    public ArrayList<String> getStaticGroupMembers(String groupName) {
        return groupManagement.getStaticGroupMembers(groupName);
    }

    public boolean canJoinStaticGroup(String group, String address){
        return groupManagement.canJoinStaticGroup(group, address);
    }
    public void canStartSendingMessagesCheck(String group){
        if (groupManagement.canStartSendingMessages(group)) {
            groupManagement.addCanSendMessages(group);
        }
    }

    public DebugMonitor getDebugMonitor() {
        return debugMonitor;
    }

    public void leaveAllGroups(){
        List<String> groupNames = groupManagement.getGroupNames();
        for (String groupName : groupNames){
            leaveGroup(groupName);
        }
    }

    public void removeMember(String groupName, String address) {
        groupManagement.removeMember(groupName, address);
    }
}
