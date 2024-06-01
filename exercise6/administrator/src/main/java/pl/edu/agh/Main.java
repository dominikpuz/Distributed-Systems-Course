package pl.edu.agh;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Main {
    private static final String EXCHANGE_NAME = "HOSPITAL_EXCHANGE";
    private static final String TOPIC = "HOSPITAL";
    private static final String ADMIN_EXCHANGE = "ADMIN_EXCHANGE";
    private static final String NAME = "ADMIN";

    public static void main(String[] args) throws IOException, TimeoutException {
        System.out.println(NAME);
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, TOPIC + ".#");
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Received:\n" + message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(queueName, false, consumer);

        Channel adminChannel = connection.createChannel();
        adminChannel.exchangeDeclare(ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);
        String adminQueueName = adminChannel.queueDeclare().getQueue();
        adminChannel.queueBind(adminQueueName, ADMIN_EXCHANGE, NAME);

        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Send to: ");
            String name = br.readLine().toUpperCase();
            if ("EXIT".equals(name)) {
                break;
            }
            System.out.println("Message: ");
            String message = br.readLine();
            adminChannel.basicPublish(ADMIN_EXCHANGE, name, null, message.getBytes());
        }

    }
}