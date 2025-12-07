package com.kittyscan.server;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KittyDash {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ArrayList<String> ips = new ArrayList<>();

    private static int reportedIpCount = 0;

    private static Integer reportSeconds = 120;

    private static UUID serverId = UUID.randomUUID();

    public static void run() {
        File idFile = new File(".kitty-id");
        if (idFile.exists()) {
            try {
                var idString = java.nio.file.Files.readString(idFile.toPath()).trim();
                serverId = UUID.fromString(idString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                java.nio.file.Files.writeString(idFile.toPath(), serverId.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("****************************************************************************");
        LOGGER.info("*                                                                          *");
        LOGGER.info("*  KittyDash is enabled! Reporting blocked IPs every {} seconds.          *", reportSeconds);
        LOGGER.info("*  Visit https://kittypaper.com/dash/{}  *", serverId.toString());
        LOGGER.info("*  to view your server's dashboard.                                        *");
        LOGGER.info("*                                                                          *");
        LOGGER.info("****************************************************************************");

        var timer = new java.util.Timer();

        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                var i = getIps();
                URL url = null;
                try {
                    url = new URL("https://kittypaper.com/api/report");
                    URLConnection con = url.openConnection();
                    HttpURLConnection http = (HttpURLConnection)con;
                    http.setRequestMethod("POST");
                    http.setDoOutput(true);
                    http.setRequestProperty("Content-Type", "application/json");

                    String blockedIps = "[\"" + String.join("\",\"", i) + "\"]";
                    String jsonPayload = "{\"id\":\"" + serverId.toString() + "\",\"blockedIps\": " +  blockedIps + ",\"reportedIps\": " + getReportedIpCount() + "}";
                    byte[] out = jsonPayload.getBytes();
                    http.setFixedLengthStreamingMode(out.length);
                    http.connect();
                    try(var os = http.getOutputStream()) {
                        os.write(out);
                    }
                    int responseCode = http.getResponseCode();
                    if (responseCode != 200) {
                        LOGGER.error("Failed to report IPs to KittyDash. Response code: {}", responseCode);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, reportSeconds * 1000);
    }

    public static synchronized void addIp(String ip) {
        ips.add(ip);
    }

    public static synchronized List<String> getIps() {
        var i = new ArrayList<>(ips);

        ips.clear();

        return i;
    }

    public static UUID getServerId() {
        return serverId;
    }

    public static synchronized void addReportedIpCount(int count) {
        reportedIpCount += count;
    }

    private static synchronized int getReportedIpCount() {
        int count = reportedIpCount;
        reportedIpCount = 0;

        return reportedIpCount;
    }
}
