package se.gcom.middleware.communicationModule;

import io.grpc.stub.StreamObserver;
import se.gcom.middleware.Manager;

import java.util.Map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CommunicationGrpcHandler extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final Manager manager;
    private final Set<String> seenMessages;
    private final HashMap<String, Boolean> groupToReliable;
    public CommunicationGrpcHandler(Manager manager) {
        this.manager = manager;
        seenMessages = new HashSet<>();
        groupToReliable = new HashMap<>();
    }

    @Override
    public void sendMessage(Message msg, StreamObserver<Ack> responseObserver) {

        switch (msg.getContentCase()){
            case CHATMESSAGE:
                ChatMessage chatMessage = msg.getChatMessage();
                if(!seenMessages.contains(chatMessage.getMessageId())){
                    // incomingMessage uses the OrderingModule
                    seenMessages.add(chatMessage.getMessageId());
                    manager.handleIncomingMessage(chatMessage, groupToReliable.get(chatMessage.getGroupId()));
                }

                System.out.println("Received: " + chatMessage.getPayload());
                System.out.println(chatMessage.getSenderId());
                System.out.println(chatMessage.getGroupId());
                System.out.println(chatMessage.getMessageId());
                break;

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
        }

        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
    public void addGroupToReliablePairing(String group, boolean reliable){
        groupToReliable.put(group, reliable);
    }
}