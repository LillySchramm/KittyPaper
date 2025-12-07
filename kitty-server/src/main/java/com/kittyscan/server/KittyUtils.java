package com.kittyscan.server;

public class KittyUtils {
    public static boolean isIpV4(String ip) {
        return ip.split("\\.").length == 4;
    }
}
