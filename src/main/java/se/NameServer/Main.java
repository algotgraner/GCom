package se.NameServer;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(1111).addService(new NamingServerGrpcHandler()).build();
        System.out.println("Server started on port 1111");
        server.start();
        server.awaitTermination();
    }
}
