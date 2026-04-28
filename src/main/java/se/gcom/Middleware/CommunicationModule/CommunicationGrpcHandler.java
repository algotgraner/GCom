package se.gcom.Middleware.CommunicationModule;

import io.grpc.stub.StreamObserver;

public class CommunicationGrpcHandler extends CommunicationServiceGrpc.CommunicationServiceImplBase {
    @Override
    public void sendMessage(ChatMessage request, StreamObserver<Ack> responseObserver) {

        // 1. 

        System.out.println("Received: " + request.getPayload());
        System.out.println(request.getSenderId());
        System.out.println(request.getReceiverId());
        System.out.println(request.getMessageId());

        Ack ack = Ack.newBuilder().setSuccess(true).build();
        responseObserver.onNext(ack);
        responseObserver.onCompleted();
    }
}
