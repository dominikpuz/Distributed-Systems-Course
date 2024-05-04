package pl.agh.edu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pl.agh.edu.gen.ExecutionRequest;
import pl.agh.edu.gen.ExecutionResponse;
import pl.agh.edu.gen.ExecutionServiceGrpc;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {
    private ManagedChannel channel;
    private ExecutionServiceGrpc.ExecutionServiceBlockingStub executionServiceStub;

    private final String jarLocation = "target/server-1.0-SNAPSHOT.jar";

    public Main(String remoteHost, int remotePort) {
        channel = ManagedChannelBuilder.forAddress(remoteHost, remotePort)
                .usePlaintext() // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid needing certificates.
                .build();

        executionServiceStub = ExecutionServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        Main client = new Main("127.0.0.2", 10000);
        client.test();
    }

    public void test() throws InterruptedException {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        
        System.out.println("Test hello from server");
        ExecutionRequest request1 = ExecutionRequest.newBuilder()
                .setJarLocation(jarLocation)
                .setMethod("pl.agh.edu.ExampleMethods:helloFromServer")
                .setData("")
                .build();
        ExecutionResponse response1 = executionServiceStub.execute(request1);
        System.out.println("Response: " + gson.fromJson(response1.getData(), String.class));
        System.out.println("========================================================");

        System.out.println("Test avg");
        List<Double> list = Arrays.asList(1.23, 3.21, 5.31, 6.39);
        ExecutionRequest request2 = ExecutionRequest.newBuilder()
                .setJarLocation(jarLocation)
                .setMethod("pl.agh.edu.ExampleMethods:avg")
                .setData(gson.toJson(list))
                .build();
        ExecutionResponse response2 = executionServiceStub.execute(request2);
        System.out.println("Response: " + gson.fromJson(response2.getData(), Double.class));
        System.out.println("========================================================");

        System.out.println("Test sum rectangles");
        ExecutionRequest request3 = ExecutionRequest.newBuilder()
                .setJarLocation(jarLocation)
                .setMethod("pl.agh.edu.ExampleMethods:sumRectangles")
                .setData("[{\"x\":43.12,\"y\":23.12},{\"x\":51.43,\"y\":53.12},{\"x\":61.43,\"y\":63.12}]")
                .build();
        ExecutionResponse response3 = executionServiceStub.execute(request3);
        System.out.println("Response: " + gson.fromJson(response3.getData(), Double.class));
        System.out.println("========================================================");

        shutdown();
    }
}