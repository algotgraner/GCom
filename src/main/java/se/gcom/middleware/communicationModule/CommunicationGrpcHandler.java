package se.gcom.middleware.communicationModule;

import io.grpc.stub.StreamObserver;
import se.gcom.middleware.Manager;

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
                manager.receiveMessage(chatMessage);

                System.out.println("Received: " + chatMessage.getPayload());
                System.out.println(chatMessage.getSenderId());
                System.out.println(chatMessage.getReceiverId());
                System.out.println(chatMessage.getMessageId());
                break;

            case GROUPMEMBERSHIP:
                GroupMembership groupMembership = msg.getGroupMembership();
                manager.receiveMessage(groupMembership);
                break;
        }

        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}