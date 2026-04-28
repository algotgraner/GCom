package se.gcom.Middleware.CommunicationModule;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ReceiveTest {
    public static void main(String[] args) throws Exception {

        Server server = ServerBuilder
                .forPort(50051)
                .addService(new CommunicationGrpcHandler())
                .build();

        server.start();
        System.out.println("Listening on port 50051");
        server.awaitTermination();

    }
}
