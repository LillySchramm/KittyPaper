package com.kittyscan.server.honeypot.message;

import com.kittyscan.server.honeypot.Config;
import com.kittyscan.server.honeypot.message.handlers.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class MessageHandler {
    public Channel channel;
    private Connection connection;

    public final Queue pingQueue;
    public final Queue requestQueue;
    public final Queue joinQueue;

    public MessageHandler(Config config) throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        this.connect(config);

        this.pingQueue = new Queue(this.channel, "mc.raw_honeypot_ping", "mc.raw_honeypot_ping.");
        this.requestQueue = new Queue(this.channel, "mc.raw_honeypot_request", "mc.raw_honeypot_request.");
        this.joinQueue = new Queue(this.channel, "mc.raw_honeypot_join", "mc.raw_honeypot_join.");
    }

    private void connect(Config config) throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.RABBITMQ_HOST);
        factory.setPassword(config.RABBITMQ_PASSWORD);
        factory.setUsername(config.RABBITMQ_USERNAME);
        factory.setVirtualHost(config.RABBITMQ_VIRTUAL_HOST);
        factory.setPort(config.RABBITMQ_PORT);
        if (config.RABBITMQ_USE_SSL) {
            factory.useSslProtocol();
        }
        factory.setRequestedHeartbeat(5);

        Connection connection = factory.newConnection();
        this.connection = connection;

        this.channel = connection.createChannel();
    }

    public void close() throws IOException, TimeoutException {
        this.connection.close();
    }
}
