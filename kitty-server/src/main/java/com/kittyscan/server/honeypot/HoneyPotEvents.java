package com.kittyscan.server.honeypot;

public interface HoneyPotEvents {
    void requestReceived(String ip);
    void playerJoined(String uuid, String username, String ip);
}
