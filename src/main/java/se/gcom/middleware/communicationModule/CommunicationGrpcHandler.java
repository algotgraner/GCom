package se.gcom.middleware.communicationModule;

import io.grpc.stub.StreamObserver;
import se.gcom.middleware.Manager;

import java.util.HashSet;
import java.util.Set;

public class CommunicationGrpcHandler extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final Manager manager;
    private final Set<String> seenMessages;
    public CommunicationGrpcHandler(Manager manager) {
        this.manager = manager;
        seenMessages = new HashSet<>();
    }

    @Override
    public void sendMessage(Message msg, StreamObserver<Ack> responseObserver) {

        switch (msg.getContentCase()){
            case CHATMESSAGE:
                ChatMessage chatMessage = msg.getChatMessage();
                if(!seenMessages.contains(chatMessage.getMessageId())){
                    // incomingMessage uses the OrderingModule
                    seenMessages.add(chatMessage.getMessageId());
                    manager.handleIncomingMessage(chatMessage);
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
                break;
        }

        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}