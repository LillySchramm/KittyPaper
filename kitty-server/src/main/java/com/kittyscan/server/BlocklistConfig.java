package com.kittyscan.server;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BlocklistConfig {
    public final String url;
    public final int refreshIntervalMinutes;
    public final int subnetMask;

    public ArrayList<String> blocklistedIPs = new ArrayList<>();

    public BlocklistConfig(String url, int refreshIntervalMinutes, int subnetMask) {
        this.url = url;
        this.refreshIntervalMinutes = refreshIntervalMinutes;
        this.subnetMask = subnetMask;
    }

    public boolean isBlocklisted(String ip) {
        // We do not support IPv6 for blocklisting at the moment
        if (ip.split("\\.").length != 4) {
            return false;
        }

        for (String blocklistedIP : blocklistedIPs) {
            boolean isInSubnet = false;
            try {
                isInSubnet = ipInSubnet(ip, blocklistedIP + "/" + subnetMask);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

            if (isInSubnet) {
                return true;
            }
        }

        return false;
    }

    // https://stackoverflow.com/a/77692176
    public boolean ipInSubnet(String ip, String subnet) throws UnknownHostException {
        int maskSize = Integer.parseInt(subnet.split("/")[1]);
        final InetAddress subnetAddress = InetAddress.getByName(subnet.split("/")[0]);
        final InetAddress address = InetAddress.getByName(ip);
        int maxMaskSize = subnetAddress instanceof Inet4Address ? 32 : 128;
        final BigInteger maskBits = BigInteger.ONE.shiftLeft(maskSize).subtract(BigInteger.ONE).shiftLeft(maxMaskSize - maskSize);
        return new BigInteger(address.getAddress()).xor(new BigInteger(subnetAddress.getAddress())).and(maskBits).equals(BigInteger.ZERO);
    }

    public void update() {
        try {
            URL url = new URL(this.url);
            URLConnection conn = null;
            conn = url.openConnection();
            InputStream is = conn.getInputStream();
            String content = new String(is.readAllBytes());
            String[] lines = content.split("\n");

            blocklistedIPs.clear();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                blocklistedIPs.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        update();
                    }
                },
                (long) refreshIntervalMinutes * 60 * 1000
        );
    }
}
