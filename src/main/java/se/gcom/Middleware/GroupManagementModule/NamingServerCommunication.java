package se.gcom.Middleware.GroupManagementModule;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import se.NameServer.grpc.Empty;
import se.NameServer.grpc.NamingServiceGrpc;

public class NamingServerCommunication {
    private ManagedChannel channel;
    NamingServiceGrpc.NamingServiceBlockingStub stub;
    public NamingServerCommunication(){
        channel = ManagedChannelBuilder
                .forAddress("localhost", 1111)
                .usePlaintext()
                .build();
        stub = NamingServiceGrpc.newBlockingStub(channel);
    }
    public boolean isUp(){
        try {
            stub.isUp(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            return false;
        }
        return true;
    }
    public void shutdown(){
        channel.shutdown();
    }
}
