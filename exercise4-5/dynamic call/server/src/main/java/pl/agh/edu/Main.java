package pl.agh.edu;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

public class Main {
    private final String address = "127.0.0.2";
    private final int port = 10000;
    private Server server;

    private SocketAddress socketAddress;

    private void start() throws IOException {
        try {
            socketAddress = new InetSocketAddress(InetAddress.getByName(address), port);
        } catch (UnknownHostException e) {}
        server = ServerBuilder.forPort(port).executor((Executors.newFixedThreadPool(16)))
                .addService(new ExecutionServiceImpl())
                .build()
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("Shutting down gRPC server...");
                Main.this.stop();
                System.err.println("Server shut down.");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final Main main = new Main();
        main.start();
        main.blockUntilShutdown();
    }
}