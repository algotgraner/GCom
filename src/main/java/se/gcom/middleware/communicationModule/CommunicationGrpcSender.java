package se.gcom.middleware.communicationModule;

import java.util.List;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class CommunicationGrpcSender {
    //this class will be used to make grpc calls to send messages to other nodes

    public void multicast(ChatMessage msg, List<Integer> ports){
        for (Integer port : ports){
            sendToNode("localhost", port, msg);
        }
    }

    private void sendToNode(String host, int port, ChatMessage msg){
        try {
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(host, port)
                    .usePlaintext()
                    .build();

            CommunicationServiceGrpc.CommunicationServiceBlockingStub stub =
                    CommunicationServiceGrpc.newBlockingStub(channel);

            // send the message to the node
            Ack ack = stub.sendMessage(msg);
            System.out.println("ACK status: " + ack);

            channel.shutdown();
        } catch (Exception e) {
            System.err.println("Could not reach node at port " + port + ": " + e.getMessage());
        }
    }

}