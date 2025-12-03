package com.kittyscan.server.honeypot.message.handlers;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public class Queue {
    private final String exchangeName;
    private final Channel channel;
    private final String baseRoutingKey;

    public Queue(Channel channel, String exchangeName, String baseRoutingKey) throws IOException {
        this.channel = channel;

        this.exchangeName = exchangeName;
        this.baseRoutingKey = baseRoutingKey;
    }

    public void publish(String message) throws IOException {
        publish(message, "");
    }

    public void publish(String message, String routingKey) throws IOException {
        if (!channel.isOpen()) {
            return;
        }

        channel.basicPublish(exchangeName, baseRoutingKey + routingKey, null, message.getBytes());
    }
}
