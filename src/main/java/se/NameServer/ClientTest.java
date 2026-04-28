package se.NameServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import se.NameServer.grpc.*;

import java.util.ArrayList;
import java.util.List;

public class ClientTest {
    public static void main(String[] args) {
        System.out.println("Started test client");

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 1111)
                .usePlaintext()
                .build();

        NamingServiceGrpc.NamingServiceBlockingStub stub =
                NamingServiceGrpc.newBlockingStub(channel);

        // 1. Add a group
        try {
            stub.addNewGroup(
                    AddGroupRequest.newBuilder()
                            .setGroupName("group1")
                            .addAddresses("127.0.0.1:5000")
                            .addAddresses("127.0.0.1:5001")
                            .build()
            );
        } catch (StatusRuntimeException e){
            System.out.println(e.getMessage());
        }

        // 2. Add one more address
        try {
            stub.addToGroup(
                    AddToGroupRequest.newBuilder()
                            .setGroupName("group12")
                            .setAddress("127.0.0.1:5002")
                            .build()
            );
        }catch (StatusRuntimeException e){
            System.out.println(e.getMessage());
        }

        try {
            stub.removeFromGroup(AddToGroupRequest.newBuilder().setGroupName("group12").setAddress("127.0.0.1:5001").build());
        } catch (StatusRuntimeException e){
            System.out.println(e.getMessage());
        }
        // 3. Get addresses
        List<String> addresses = new ArrayList<>();
        try {
            AddressList response = stub.getAddresses(
                    GroupRequest.newBuilder()
                            .setGroupName("group12")
                            .build()
            );
            addresses = response.getAddressesList();
        } catch (StatusRuntimeException e){
            System.out.println(e.getMessage());
        }


        System.out.println("Addresses:");
        for (String a : addresses) {
            System.out.println(a);
        }

        channel.shutdown();
    }
}
