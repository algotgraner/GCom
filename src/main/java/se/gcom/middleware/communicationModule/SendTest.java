package se.gcom.middleware.communicationModule;

import java.util.List;
import io.grpc.*;

public class SendTest {
    public static void main(String[] args) throws Exception {
        CommunicationGrpcSender sender = new CommunicationGrpcSender();

        ChatMessage msg = ChatMessage.newBuilder()
                .setMessageId("1")
                .setSenderId("Jonis")
                .setReceiverId("TestSender")
                .setPayload("Hello from Jonis")
                .build();

        sender.multicast(msg, List.of(5001, 5002));
    }
}