package se.gcom.middleware.communicationModule;

import io.grpc.stub.StreamObserver;
import se.gcom.middleware.Manager;

public class CommunicationGrpcHandler extends CommunicationServiceGrpc.CommunicationServiceImplBase {

    private final Manager manager;
    public CommunicationGrpcHandler(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void sendMessage(ChatMessage msg, StreamObserver<Ack> responseObserver) {

        manager.receiveMessage(msg);

        System.out.println("Received: " + msg.getPayload());
        System.out.println(msg.getSenderId());
        System.out.println(msg.getReceiverId());
        System.out.println(msg.getMessageId());

        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}