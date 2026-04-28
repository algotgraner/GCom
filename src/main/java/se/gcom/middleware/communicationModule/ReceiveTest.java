package se.gcom.middleware.communicationModule;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ReceiveTest {
    public static void main(String[] args) throws Exception {

        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5001;

        Server server = ServerBuilder
                .forPort(port)
                .addService(new CommunicationGrpcHandler())
                .build();

        server.start();
        System.out.println("Listening on port:" + port);
        server.awaitTermination();

    }
}