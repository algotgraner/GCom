package se.gcom.middleware.communicationModule;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import se.gcom.middleware.Manager;
import java.util.List;

public class CommunicationService {
    private final Manager manager;
    private final CommunicationGrpcSender sender;
    private final CommunicationGrpcHandler handler;
    private Server server;

    public CommunicationService(Manager manager) {
        this.manager = manager;
        this.sender = new CommunicationGrpcSender(manager);
        this.handler = new CommunicationGrpcHandler(manager);
    }

    public void start(int port) {
        try {
            server = ServerBuilder.forPort(port)
                    .addService(handler)
                    .build()
                    .start();
            System.out.println("CommunicationService listening on port " + port);
        } catch (Exception e) {
            throw new RuntimeException("Failed to start server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void multicast(ChatMessage msg, List<String> addresses) {
        sender.multicast(msg, addresses);
    }
}
