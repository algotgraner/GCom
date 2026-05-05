package se.gcom.middleware.communicationModule;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import se.gcom.middleware.Manager;

public class CommunicationGrpcSender {
    //this class will be used to make grpc calls to send messages to other nodes
    private final ConcurrentHashMap<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    private final Manager manager;
    public CommunicationGrpcSender(Manager manager) {
        this.manager = manager;
    }

    public void multicast(Message msg, List<String> addresses){
        for (String address : addresses) {
            sendToNode(address, msg);

        }
    }

    private void sendToNode(String address, Message msg){
        ManagedChannel channel = getChannel(address);
        try {
            CommunicationServiceGrpc.CommunicationServiceBlockingStub stub =
                    CommunicationServiceGrpc.newBlockingStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS);

            // send the message to the node
            Ack ack = stub.sendMessage(msg);
            System.out.println("ACK status: " + ack);

        } catch (StatusRuntimeException e){
            System.err.println("GRPC runtime exception, Removing the address, (SHOULD CALL MANAGER HERE AND REPORT FAILURE):" + e.getMessage());
            removeChannel(address);
        } catch (Exception e) {
            System.err.println("Could not reach node: " + e.getMessage());
            removeChannel(address);
        }
    }

    private ManagedChannel getChannel(String address){
        return channelMap.computeIfAbsent(address, addr -> {
            String[] parts = addr.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            return ManagedChannelBuilder.forAddress(host, port).usePlaintext().keepAliveWithoutCalls(true).build();
        });
    }

    private void removeChannel(String address){
        ManagedChannel channel = channelMap.remove(address);
        if (channel != null){
            channel.shutdown();
            System.out.println("Removed channel for address:" + address);
        }

    }

    public Ack sendJoinRequest(Message msg, String address) {
        ManagedChannel channel = getChannel(address);
        try {
            CommunicationServiceGrpc.CommunicationServiceBlockingStub stub =
                    CommunicationServiceGrpc.newBlockingStub(channel)
                            .withDeadlineAfter(10, TimeUnit.SECONDS);

            return stub.sendMessage(msg);

        } catch (Exception e) {
            System.err.println("Failed to get response from " + address + ": " + e.getMessage());
            removeChannel(address);
            return Ack.newBuilder().setSuccess(false).build();
        }
    }

    // This function should be called on shutdown
    public void shutdown(){
        System.out.println("Shutting down all channels...");
        channelMap.forEach((addr, ch) -> ch.shutdownNow());
        channelMap.clear();
    }

}