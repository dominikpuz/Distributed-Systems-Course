package pl.edu.agh;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class Main {
    private static final String EXCHANGE_NAME = "HOSPITAL_EXCHANGE";
    private static final String TOPIC = "HOSPITAL";
    private static final String ADMIN_EXCHANGE = "ADMIN_EXCHANGE";
    private static final String NAME = System.getenv("name");

    public static void main(String[] args) throws IOException, TimeoutException {
        StringBuilder specialization = new StringBuilder();
        for (String arg : args) {
            specialization.append(arg + " ");
        }
        System.out.println(NAME + " " + specialization);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        List<Channel> channels = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();
        for (String arg : args) {
            Channel channel = connection.createChannel();
            channel.basicQos(1);
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            channel.queueDeclare(arg, false, false, false, null);
            channel.queueBind(arg, EXCHANGE_NAME, TOPIC + "." + arg);
            channels.add(channel);
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    Random random = new Random();
                    String test = new String(body, "UTF-8");
                    System.out.println("Received test:\n" + test);
                    try {
                        Thread.sleep(random.nextInt(1000, 5000));
                        String response = test + "\ndone";
                        AMQP.BasicProperties replyProperties = new AMQP.BasicProperties
                                .Builder()
                                .correlationId(properties.getCorrelationId())
                                .build();

                        channel.basicPublish("", properties.getReplyTo(), replyProperties, response.getBytes("UTF-8"));
                        channel.basicPublish(EXCHANGE_NAME, TOPIC, null, response.getBytes("UTF-8"));
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            consumers.add(consumer);
            channel.basicConsume(arg, false, consumer);
        }
        Channel adminChannel = connection.createChannel();
        adminChannel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);
        String adminQueueName = adminChannel.queueDeclare().getQueue();
        adminChannel.queueBind(adminQueueName, ADMIN_EXCHANGE, NAME);

        Consumer adminConsumer = new DefaultConsumer(adminChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String response = new String(body, "UTF-8");
                System.out.println("\nReceived from admin:\n" + response);
            }
        };

        adminChannel.basicConsume(adminQueueName, true, adminConsumer);
    }
}