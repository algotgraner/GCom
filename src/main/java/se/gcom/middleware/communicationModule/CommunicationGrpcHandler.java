package se.gcom.middleware.communicationModule;

import io.grpc.stub.StreamObserver;
import se.gcom.middleware.Manager;

import java.util.Map;

public class CommunicationGrpcHandler extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final Manager manager;
    public CommunicationGrpcHandler(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void sendMessage(Message msg, StreamObserver<Ack> responseObserver) {

        switch (msg.getContentCase()){
            case CHATMESSAGE:
                ChatMessage chatMessage = msg.getChatMessage();
                // incomingMessage uses the OrderingModule
                manager.handleIncomingMessage(chatMessage);

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
                    if (manager.orderingIsCausal(groupMembership.getGroupId())){
                        // the group is causally ordered so we need to append the vector clock to the ack
                        Map<String, Integer> currentVC = manager.getCurrentVectorClock(groupMembership.getGroupId());
                        // joining message, respond ack with vector clock
                        MembershipAck membershipAck = MembershipAck.newBuilder().putAllVectorClock(currentVC).build();
                        // pack into the ack
                        Ack ack = Ack.newBuilder()
                                .setSuccess(true)
                                .setMembership(membershipAck)
                                .build();

                        responseObserver.onNext(ack);
                        responseObserver.onCompleted();
                        return;
                    }
                }
                break;
        }
        // Normal ack
        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}