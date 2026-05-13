package se.gcom.middleware.communicationModule;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import se.gcom.app.debug.DebugEventType;
import se.gcom.middleware.Manager;

public class CommunicationGrpcSender {
    //this class will be used to make grpc calls to send messages to other nodes
    private final ConcurrentHashMap<String, ManagedChannel> channelMap = new ConcurrentHashMap<>();

    private final Manager manager;
    public CommunicationGrpcSender(Manager manager) {
        this.manager = manager;
    }

    public Ack multicast(Message msg, List<String> addresses){
        Ack latestAck = null;
        int dataMessages = 0;
        int ackMessages = 0;
        boolean success = true;
        String errorMessage = "";

        for (String address : addresses) {
            latestAck = sendToNode(address, msg);
            dataMessages++;
            ackMessages++;
            if (latestAck.hasData()){
                ///
            }

            if (latestAck != null) {
                dataMessages += latestAck.getDataMessages();
                ackMessages += latestAck.getAckMessages();
                success = success && latestAck.getSuccess();
                if (!latestAck.getErrorMessage().isBlank()) {
                    errorMessage = latestAck.getErrorMessage();
                }
            } else {
                success = false;
            }
        }

        Ack.Builder aggregateAck = latestAck == null
                ? Ack.newBuilder()
                : latestAck.toBuilder();

        aggregateAck
                .setSuccess(success)
                .setDataMessages(dataMessages)
                .setAckMessages(ackMessages);

        if (!errorMessage.isBlank()) {
            aggregateAck.setErrorMessage(errorMessage);
        }

        return aggregateAck.build();
    }

    private Ack sendToNode(String address, Message msg){
        ManagedChannel channel = getChannel(address);
        try {
            CommunicationServiceGrpc.CommunicationServiceBlockingStub stub =
                    CommunicationServiceGrpc.newBlockingStub(channel).withDeadlineAfter(5, TimeUnit.SECONDS);

            // send the message to the node
            recordNetworkEvent(DebugEventType.NETWORK_SEND, address, msg);
            Ack ack = stub.sendMessage(msg);
            recordAck(address, ack);
            System.out.println("ACK status: " + ack);
            return ack;

        } catch (StatusRuntimeException e){
            System.err.println("GRPC runtime exception, Removing the address, (SHOULD CALL MANAGER HERE AND REPORT FAILURE):" + e.getMessage());
            manager.handleNodeFailure(msg.getChatMessage().getGroupId(), address);
            removeChannel(address);
            return Ack.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("gRPC error: " + e.getStatus().getCode() + " - " + e.getMessage())
                    .build();

        } catch (Exception e) {
            System.err.println("Could not reach node: " + e.getMessage());
            removeChannel(address);
            return Ack.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("gRPC error: " + e.getMessage() + " - " + e.getMessage())
                    .build();
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

            recordNetworkEvent(DebugEventType.NETWORK_SEND, address, msg);
            Ack ack = stub.sendMessage(msg);
            recordAck(address, ack);
            return ack;

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

    private void recordNetworkEvent(DebugEventType type, String address, Message msg) {
        manager.getDebugMonitor().recordEvent(
                type,
                manager.getMyAddress(),
                "to=" + address + " content=" + msg.getContentCase()
        );
    }

    private void recordAck(String address, Ack ack) {
        manager.getDebugMonitor().recordEvent(
                DebugEventType.NETWORK_ACK,
                manager.getMyAddress(),
                "from=" + address + " success=" + ack.getSuccess()
        );
    }

}
