package com.dhiep.syncket.sockets;

import com.dhiep.syncket.Syncket;
import com.dhiep.syncket.events.CustomSyncketEvent;
import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.dhiep.syncket.models.SyncketType;
import com.dhiep.syncket.utils.LogUtil;
import com.dhiep.syncket.utils.MessageUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
        identifier = config.getString("identifier", "syncket");
        password = config.getString("password", "password");
    }

    public static void start() {
        if (task != null) stop();
        try {
            switch (type) {
                case SERVER:
                    ServerSocket server = new ServerSocket();
                    task = new SyncketServerTask(server);
                    task.runTaskAsynchronously(Syncket.getInstance());
                    LogUtil.debug("Started syncket task type SERVER");
                    break;
                case CLIENT:
                    Socket client = new Socket();
                    task = new SyncketClientTask(client);
                    task.runTaskAsynchronously(Syncket.getInstance());
                    LogUtil.debug("Started syncket task type CLIENT");
                    break;
            }
        } catch (IOException exception) {
            LogUtil.severe("Cannot start syncket task!");
            exception.printStackTrace();
        }
    }

    public static boolean send(SendMode mode, ActionType action, String target, JsonElement data) {
        return task.send(mode, action, target, data);
    }

    public static void stop() {
        task.stop();
    }

    public static void execute(ActionType type, JsonElement data) {
        Bukkit.getScheduler().runTask(Syncket.getInstance(), () -> {
            switch (type) {
                case BROADCAST:
                    MessageUtil.broadcast(data.getAsString());
                    break;
                case COMMAND:
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), data.getAsString());
                    break;
                case EVENT:
                    CustomSyncketEvent event = new CustomSyncketEvent(identifier, data);
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

    public static String getIdentifier() {
        return identifier;
    }

    public static String getPassword() {
        return password;
    }
}
