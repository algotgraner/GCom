package se.NameServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import se.NameServer.grpc.*;

import java.util.List;

public class ClientTest {
    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        NamingServiceGrpc.NamingServiceBlockingStub stub =
                NamingServiceGrpc.newBlockingStub(channel);

        // 1. Add a group
        stub.addNewGroup(
                AddGroupRequest.newBuilder()
                        .setGroupName("group1")
                        .addAddresses("127.0.0.1:5000")
                        .addAddresses("127.0.0.1:5001")
                        .build()
        );

        // 2. Add one more address
        stub.addToGroup(
                AddToGroupRequest.newBuilder()
                        .setGroupName("group1")
                        .setAddress("127.0.0.1:5002")
                        .build()
        );

        // 3. Get addresses
        AddressList response = stub.getAddresses(
                GroupRequest.newBuilder()
                        .setGroupName("group1")
                        .build()
        );

        List<String> addresses = response.getAddressesList();

        System.out.println("Addresses:");
        for (String a : addresses) {
            System.out.println(a);
        }

        channel.shutdown();
    }
}
