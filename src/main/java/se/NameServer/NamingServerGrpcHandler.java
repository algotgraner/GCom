package se.NameServer;
import io.grpc.stub.StreamObserver;
import se.NameServer.grpc.*;

import java.util.ArrayList;

public class NamingServerGrpcHandler extends NamingServiceGrpc.NamingServiceImplBase {
    NamingServer namingServer = new NamingServer();

    @Override
    public void getAddresses(GroupRequest request, StreamObserver<AddressList> responseObserver) {
        ArrayList<String> addresses = namingServer.getAddresses(request.getGroupName());
        if(addresses == null){
            addresses = new ArrayList<>();
        }
        AddressList addressList = AddressList.newBuilder().addAllAddresses(addresses).build();
        responseObserver.onNext(addressList);
        responseObserver.onCompleted();
    }

    @Override
    public void addNewGroup(AddGroupRequest request, StreamObserver<Empty> responseObserver) {
        String groupName = request.getGroupName();
        ArrayList<String> addresses = new ArrayList<>(request.getAddressesList());
        namingServer.addNewGroup(groupName, addresses);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void addToGroup(AddToGroupRequest request, StreamObserver<Empty> responseObserver){
        String groupName = request.getGroupName();
        String address = request.getAddress();
        namingServer.addToGroup(groupName, address);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
