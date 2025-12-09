package com.kittyscan.server;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class KittyStats {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int BLOCKED_IP_CACHE_TIME_SECONDS = 60 * 60;
    public static final int REPORT_WAIT_TIME = 30 * 60;

    private static final ArrayList<BlockedIPItem> blockedIPs = new ArrayList<>();

    private static final ArrayList<WatchedIPItem> watchedIPs = new ArrayList<>();

    public static final File JOINED_IPS_FILE = new File("kittylog/kittypaper-joined-ips.txt");

    public static ArrayList<String> joinedIPsCache = null;

    public static void run() {
        if (!JOINED_IPS_FILE.getParentFile().exists()) {
            JOINED_IPS_FILE.getParentFile().mkdirs();
        }

        if (!JOINED_IPS_FILE.exists()) {
            try {
                JOINED_IPS_FILE.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        joinedIPsCache = getJoinedIPs();

        var timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                processWatchedIPs();
            }
        }, 0, 300 * 1000);
    }

    public static void logJoinedIP(String ip) {
        if (!KittyConfig.enableSuspiciousReporting) {
            return;
        }

        if (joinedIPsCache == null) {
            joinedIPsCache = getJoinedIPs();
        }

        if (!joinedIPsCache.contains(ip)) {
            joinedIPsCache.add(ip);
            saveJoinedIPs(joinedIPsCache);

            if (KittyConfig.verbose) {
                LOGGER.info("Logged newly joined IP: {}", ip);
            }
        }
    }

    private static synchronized ArrayList<String> getJoinedIPs() {
        ArrayList<String> joinedIPs = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(JOINED_IPS_FILE.toPath());
            joinedIPs.addAll(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return joinedIPs;
    }

    private static synchronized void saveJoinedIPs(List<String> joinedIPs) {
        try {
            Files.write(JOINED_IPS_FILE.toPath(), joinedIPs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static synchronized void processWatchedIPs() {
        if (KittyConfig.verbose) {
            LOGGER.info("Processing watched IPs...");
        }

        long currentTime = System.currentTimeMillis() / 1000L;

        ArrayList<WatchedIPItem> toRemove = new ArrayList<>();
        ArrayList<String> toReport = new ArrayList<>();
        for (WatchedIPItem item : watchedIPs) {
            if ((currentTime - item.timestamp) > REPORT_WAIT_TIME) {
                if (KittyConfig.verbose) {
                    LOGGER.info("Ip {} has been watched for {} seconds, checking if suspicious...", item.ip, (currentTime - item.timestamp));
                }

                if (!joinedIPsCache.contains(item.ip)) {
                    if (KittyConfig.verbose) {
                        LOGGER.info("Ip {} is suspicious, adding to report list.", item.ip);
                    } else {
                        LOGGER.info("Ip {} is not suspicious, player has joined before.", item.ip);
                    }

                    toReport.add(item.ip);
                }
                toRemove.add(item);
            }
        }

        watchedIPs.removeAll(toRemove);

        if (!toReport.isEmpty()) {
            try {
                URL url = new URL("https://kittypaper.com/api/ip-report");
                URLConnection con = url.openConnection();
                HttpURLConnection http = (HttpURLConnection)con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type", "application/json");

                String blockedIps = "[\"" + String.join("\",\"", toReport) + "\"]";
                String jsonPayload = "{\"id\":\"" + KittyDash.getServerId().toString() + "\",\"ips\": " +  blockedIps + "}";

                if (KittyConfig.verbose) {
                    LOGGER.info("Reporting suspicious IPs to KittyDash: {}", jsonPayload);
                }

                byte[] out = jsonPayload.getBytes();
                http.setFixedLengthStreamingMode(out.length);
                http.connect();
                try(var os = http.getOutputStream()) {
                    os.write(out);
                }
                int responseCode = http.getResponseCode();
                if (responseCode != 200) {
                    LOGGER.error("Failed to report IPs to KittyDash. Response code: {}", responseCode);
                } else {
                    if (KittyConfig.verbose) {
                        LOGGER.info("Successfully reported {} suspicious IPs to KittyDash.", toReport.size());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized void addWatchedIP(String ip) {
        if (!KittyConfig.enableSuspiciousReporting) {
            return;
        }

        if (joinedIPsCache != null && joinedIPsCache.contains(ip)) {
            return;
        }

        long timestamp = System.currentTimeMillis() / 1000L;

        watchedIPs.add(new WatchedIPItem(ip, timestamp));

        if (KittyConfig.verbose) {
            LOGGER.info("Added watched IP: {}", ip);
        }
    }

    private static class WatchedIPItem {
        public final String ip;
        public final long timestamp;

        public WatchedIPItem(String ip, long timestamp) {
            this.ip = ip;
            this.timestamp = timestamp;
        }
    }

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
