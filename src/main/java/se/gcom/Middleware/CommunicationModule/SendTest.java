package se.gcom.Middleware.CommunicationModule;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class SendTest {
    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        CommunicationServiceGrpc.CommunicationServiceBlockingStub stub =
                CommunicationServiceGrpc.newBlockingStub(channel);

        ChatMessage msg = ChatMessage.newBuilder()
                .setMessageId("1")
                .setSenderId("Jonis")
                .setReceiverId("TestSender")
                .setPayload("Hello from Jonis")
                .build();

        Ack ack = stub.sendMessage(msg);

        System.out.println("Ack: " + ack.getSuccess());
        channel.shutdown();
    }
}
