package com.plexobject.util;

import java.net.InetAddress;

public class HostUtils {
    private static String localAddress;
    static {
        try {
            localAddress = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
        }
    }

    public static final String getLocalHost() {
        return localAddress;
    }
}
