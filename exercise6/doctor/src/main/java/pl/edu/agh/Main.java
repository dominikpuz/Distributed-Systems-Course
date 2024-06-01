package pl.edu.agh;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class Main {
    private static final String EXCHANGE_NAME = "HOSPITAL_EXCHANGE";
    private static final String TOPIC = "HOSPITAL";
    private static final String ADMIN_EXCHANGE = "ADMIN_EXCHANGE";
    private static final String NAME = System.getenv("name");

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println(NAME);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String correlationId = UUID.randomUUID().toString();
        String replyQueueName = "amq.rabbitmq.reply-to";
        AMQP.BasicProperties properties = new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationId)
                .replyTo(replyQueueName).build();

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                if (properties.getCorrelationId().equals(correlationId)) {
                    String response = new String(body, "UTF-8");
                    System.out.println("\nReceived:\n" + response);
                }
            }
        };

        channel.basicConsume(replyQueueName, true, consumer);

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

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter patient's name: ");
            String name = br.readLine();
            if ("exit".equalsIgnoreCase(name)) {
                break;
            }
            System.out.print("Enter test type: ");
            String test = br.readLine();
            String message = name + "\n" + test;
            channel.basicPublish(EXCHANGE_NAME, TOPIC + "." + test, properties, message.getBytes());
        }
    }
}