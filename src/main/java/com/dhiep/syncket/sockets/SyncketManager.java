package com.dhiep.syncket.sockets;

import com.dhiep.syncket.Syncket;
import com.dhiep.syncket.events.CustomSyncketEvent;
import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.dhiep.syncket.models.SyncketType;
import com.dhiep.syncket.utils.LogUtil;
import com.dhiep.syncket.utils.MessageUtil;
import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SyncketManager {
    private static boolean debug;
    private static SyncketType type;
    private static String ip;
    private static int port;
    private static int pulse;
    private static String group;
    private static String identifier;
    private static String password;

    private static SyncketRunnable task;

    public static void load() {
        Syncket instance = Syncket.getInstance();
        instance.saveDefaultConfig();
        instance.reloadConfig();
        FileConfiguration config = Syncket.getInstance().getConfig();

        debug = config.getBoolean("debug");
        try {
            type = SyncketType.valueOf(config.getString("type"));
        } catch (IllegalArgumentException exception) {
            type = SyncketType.CLIENT;
        }
        ip = config.getString("ip", "127.0.0.1");
        port = config.getInt("port", 9999);
        pulse = config.getInt("pulse", 1000);
        group = config.getString("group", "main");
        identifier = config.getString("identifier", "syncket");
        password = config.getString("password", "password");
    }

    public static void start() {
        if (task != null) stop();
        switch (type) {
            case SERVER:
                task = new SyncketServerTask();
                task.runTaskAsynchronously(Syncket.getInstance());
                LogUtil.debug("Started syncket task type SERVER");
                break;
            case CLIENT:
                task = new SyncketClientTask();
                task.runTaskAsynchronously(Syncket.getInstance());
                LogUtil.debug("Started syncket task type CLIENT");
                break;
        }
    }

    public static boolean send(SendMode mode, ActionType action, String target, JsonElement data) {
        if (task == null) return false;
        return task.send(mode, action, target, data);
    }

    public static void stop() {
        task.stop();
    }

    public static void execute(String source, ActionType type, JsonElement data) {
        Bukkit.getScheduler().runTask(Syncket.getInstance(), () -> {
            switch (type) {
                case BROADCAST:
                    MessageUtil.broadcast(data.getAsString());
                    break;
                case COMMAND:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), data.getAsString());
                    break;
                case EVENT:
                    CustomSyncketEvent event = new CustomSyncketEvent(source, data);
                    Bukkit.getPluginManager().callEvent(event);
                    break;
            }
        });
    }

    public static boolean isDebug() {
        return debug;
    }

    public static SyncketType getType() {
        return type;
    }

    public static String getIp() {
        return ip;
    }

    public static int getPort() {
        return port;
    }

    public static int getPulse() {
        return pulse;
    }

    public static String getGroup() {
        return group;
    }

    public static String getIdentifier() {
        return identifier;
    }

    public static String getPassword() {
        return password;
    }
}
