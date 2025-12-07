package com.kittyscan.server;

import java.util.ArrayList;

public class KittyStats {
    public static final int BLOCKED_IP_CACHE_TIME_SECONDS = 60 * 60;
    private static final ArrayList<BlockedIPItem> blockedIPs = new ArrayList<>();

    private static class BlockedIPItem {
        public final String ip;
        public final long timestamp;

        public BlockedIPItem(String ip, long timestamp) {
            this.ip = ip;
            this.timestamp = timestamp;
        }
    }

    public static synchronized void addBlockedIP(String ip) {
        long timestamp = System.currentTimeMillis() / 1000L;

        blockedIPs.add(new BlockedIPItem(ip, timestamp));
    }

    public static synchronized void cleanOldBlockedIPs() {
        long currentTime = System.currentTimeMillis() / 1000L;

        blockedIPs.removeIf(item -> (currentTime - item.timestamp) > BLOCKED_IP_CACHE_TIME_SECONDS);
    }

    public static synchronized int getBlockedIpCount() {
        cleanOldBlockedIPs();

        return blockedIPs.size();
    }
}
