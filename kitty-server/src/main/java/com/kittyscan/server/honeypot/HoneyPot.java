package com.kittyscan.server.honeypot;

import com.kittyscan.server.honeypot.message.MessageHandler;
import net.minecraft.server.dedicated.DedicatedServer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class HoneyPot {
    public static HoneyPotEvents events = new HoneyPotEvents() {
        @Override
        public void requestReceived(String ip) {

        }

        @Override
        public void playerJoined(String uuid, String username, String ip) {

        }
    };

    private final Config config;
    private final DedicatedServer dedicatedServer;
    @Nullable
    private MessageHandler messageHandler;

    public HoneyPot(DedicatedServer dedicatedServer) {
        this.dedicatedServer = dedicatedServer;
        config = new Config();
        if (!config.ENABLED) {
            return;
        }

        try {
            this.messageHandler = new MessageHandler(config);
        } catch (IOException | TimeoutException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        HoneyPot.events = new HoneyPotEvents() {
            @Override
            public void requestReceived(String ip) {
                try {
                    messageHandler.requestQueue.publish(config.NAME + ";" + ip);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void playerJoined(String uuid, String username, String ip) {
                try {
                    messageHandler.joinQueue.publish(config.NAME + ";" + ip + ";" + username + ";" + uuid);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        System.out.println("HoneyPot is enabled.");
    }

    public void ping() {
        if (!config.ENABLED) {
            return;
        }

        try {
            this.messageHandler.pingQueue.publish(config.NAME + ";" + dedicatedServer.getServerVersion());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        ping();
                    }
                },
                5000
        );
    }
}
