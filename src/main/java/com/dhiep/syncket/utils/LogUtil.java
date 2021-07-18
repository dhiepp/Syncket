package com.dhiep.syncket.utils;

import com.dhiep.syncket.Syncket;
import com.dhiep.syncket.sockets.SyncketManager;

public class LogUtil {
    public static void info(String message) {
        Syncket.getInstance().getLogger().info(message);
    }

    public static void warn(String message) {
        Syncket.getInstance().getLogger().warning(message);
    }

    public static void severe(String message) {
        Syncket.getInstance().getLogger().severe(message);
    }

    public static void debug(String message) {
        if (SyncketManager.isDebug()) {
            info("[Debug] " + message);
        }
    }
}
