package se.gcom.middleware.communicationModule;

import io.grpc.stub.StreamObserver;
import se.gcom.app.debug.DebugEventType;
import se.gcom.middleware.Manager;

import java.util.*;

public class CommunicationGrpcHandler extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final Manager manager;
    private final Set<String> seenMessages;
    private final HashMap<String, Boolean> groupToReliable;
    private final HashMap<String, Integer> messageCount =  new HashMap<>();
    public CommunicationGrpcHandler(Manager manager) {
        this.manager = manager;
        seenMessages = new HashSet<>();
        groupToReliable = new HashMap<>();
    }

    @Override
    public void sendMessage(Message msg, StreamObserver<Ack> responseObserver) {
        manager.getDebugMonitor().recordEvent(
                DebugEventType.NETWORK_RECEIVE,
                manager.getMyAddress(),
                "content=" + msg.getContentCase()
        );

        switch (msg.getContentCase()){
            case CHATMESSAGE:
                ChatMessage.Builder chatMessageBuilder = ChatMessage.newBuilder(msg.getChatMessage());
                if(!msg.getChatMessage().getPathList().contains(manager.getMyAddress())){
                    chatMessageBuilder.addPath(manager.getMyAddress());
                }
                ChatMessage chatMessage = chatMessageBuilder.build();
                Ack chatAck = Ack.newBuilder().setSuccess(true).build();
                if(!seenMessages.contains(chatMessage.getMessageId())){
                    messageCount.put(chatMessage.getMessageId(), 1);
                    // incomingMessage uses the OrderingModule
                    seenMessages.add(chatMessage.getMessageId());
                    chatAck = manager.handleIncomingMessage(chatMessage, groupToReliable.get(chatMessage.getGroupId()));
                }else{
                    messageCount.put(chatMessage.getMessageId(), messageCount.get(chatMessage.getMessageId())+1);
                }
                DataAck dataAck = DataAck.newBuilder().addAllPath(chatMessage.getPathList()).setMessageId(chatMessage.getMessageId()).build(); //Put these lines in an else if shortest path should be chosen (or outside the if)
                manager.sendAck(Message.newBuilder().setDatAck(dataAck).build(), chatMessage.getSenderId());

                System.out.println("Received: " + chatMessage.getPayload());
                System.out.println(chatMessage.getSenderId());
                System.out.println(chatMessage.getGroupId());
                System.out.println(chatMessage.getMessageId());
                responseObserver.onNext(chatAck);
                responseObserver.onCompleted();
                return;

            case GROUPMEMBERSHIP:
                GroupMembership groupMembership = msg.getGroupMembership();
                // here the message can be received right away
                manager.deliverIncomingMessage(groupMembership);

                if (groupMembership.getJoining()){
                    MembershipAck.Builder membershipAckBuilder = MembershipAck.newBuilder();
                    if (manager.orderingIsCausal(groupMembership.getGroupId())){
                        // the group is causally ordered so we need to append the vector clock to the ack
                        Map<String, Integer> currentVC = manager.getCurrentVectorClock(groupMembership.getGroupId());
                        // joining message, respond ack with vector clock
                        membershipAckBuilder.putAllVectorClock(currentVC).setIsCausal(true);
                    }

                    if(!manager.namingServerIsUp()){
                        membershipAckBuilder.addAllMembers(manager.getMembers(groupMembership.getGroupId()));
                    }

                    if(manager.isStaticGroup(groupMembership.getGroupId())){
                        if(manager.canJoinStaticGroup(groupMembership.getGroupId(), groupMembership.getSenderId())) {
                            membershipAckBuilder.setIsStatic(true);
                            membershipAckBuilder.setCanJoinStaticGroup(true);
                            membershipAckBuilder.addAllStaticMembers(new ArrayList<>(manager.getStaticGroupMembers(groupMembership.getGroupId())));
                            System.out.println("Sending static members:" + manager.getStaticGroupMembers(groupMembership.getGroupId()));
                        } else  {
                            membershipAckBuilder.setIsStatic(true);
                            membershipAckBuilder.setCanJoinStaticGroup(false);
                        }
                        manager.canStartSendingMessagesCheck(groupMembership.getGroupId());
                    }

                    membershipAckBuilder.setIsReliable(groupToReliable.get(groupMembership.getGroupId()));

                    MembershipAck membershipAck = membershipAckBuilder.build();
                    // pack into the ack
                    Ack ack = Ack.newBuilder()
                            .setSuccess(true)
                            .setMembership(membershipAck)
                            .build();

                    responseObserver.onNext(ack);
                    responseObserver.onCompleted();
                    return;
                }
                break;
            case DATACK:
                DataAck dataAck1 = msg.getDatAck();
                manager.receivePath(new ArrayList<>(dataAck1.getPathList()), dataAck1.getMessageId());

                Ack ack = Ack.newBuilder()
                        .setSuccess(true)
                        .build();

                responseObserver.onNext(ack);
                responseObserver.onCompleted();
                return;

        }

        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
    public void addGroupToReliablePairing(String group, boolean reliable){
        groupToReliable.put(group, reliable);
    }
    public HashMap<String, Integer> getMessageCountMap(){
        return messageCount;
    }
}
