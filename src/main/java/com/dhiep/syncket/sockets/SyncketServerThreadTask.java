package com.dhiep.syncket.sockets;

import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.dhiep.syncket.utils.JsonUtil;
import com.dhiep.syncket.utils.LogUtil;
import com.google.gson.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SyncketServerThreadTask extends BukkitRunnable {
    private SyncketServerTask server;
    private Socket client;
    private Scanner scanner;
    private PrintWriter writer;
    private String group;
    private String identifier;
    private boolean authorized;

    SyncketServerThreadTask(SyncketServerTask server, Socket client) {
        try {
            this.server = server;
            this.client = client;
            this.scanner = new Scanner(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));
            this.group = "";
            this.identifier = "";
            this.authorized = false;
        } catch (IOException exception) {
            LogUtil.severe("Cannot open client scanner!");
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        authorized = authorize();
        if (authorized) LogUtil.info("Client connected as " + identifier + " (" + group + "): " + client);
        else LogUtil.warn("Authorization failed from: " + client);

        while (scanner.hasNext()) {
            String message = scanner.nextLine();
            if (!authorized) continue;

            LogUtil.debug("Received message from client:" + message);

            JsonObject json;
            try {
                json = JsonUtil.decode(message, "mode", "action", "target", "data");
            } catch (Exception exception) {
                LogUtil.warn("Received invalid data from " + client);
                LogUtil.warn(exception.getMessage());
                continue;
            }

            try {
                SendMode mode = SendMode.valueOf(json.get("mode").getAsString());
                ActionType action = ActionType.valueOf(json.get("action").getAsString());
                String target = json.get("target").isJsonNull() ? "" : json.get("target").getAsString();
                JsonElement data = json.get("data");

                server.send(this.identifier, mode, action, target, data);
            } catch (Exception exception) {
                LogUtil.warn("Received malformed data from " + client);
                LogUtil.warn(exception.getMessage());
            }
        }
        try {
            client.close();
            LogUtil.info("Client closed: " + client);
        } catch (IOException exception) {
            LogUtil.severe("Cannot close client!");
            exception.printStackTrace();
        }
        server.remove(this);
        this.cancel();
    }

    public boolean authorize() {
        if (scanner.hasNext()) {
            String message = scanner.nextLine();

            JsonObject json;
            try {
                json = JsonUtil.decode(message, "password", "group", "identifier");
            } catch (Exception exception) {
                LogUtil.warn("Received invalid data from " + client);
                LogUtil.warn(exception.getMessage());
                return false;
            }

            LogUtil.debug("Received authorization packet: " + json);

            try {
                String password = json.get("password").getAsString();
                if (!SyncketManager.getPassword().equals(password)) return false;

                this.group = json.get("group").getAsString();
                this.identifier = json.get("identifier").getAsString();
                return true;
            } catch (Exception exception) {
                LogUtil.warn("Received malformed data from " + client + ": " + exception.getMessage());
            }
        }
        return false;
    }

    public String getGroup() {
        return group;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean send(String source, ActionType action, JsonElement data) {
        if (!authorized) return false;

        JsonObject json = new JsonObject();
        json.addProperty("source", source);
        json.addProperty("action", action.toString());
        json.add("data", data);

        LogUtil.debug("Sending packet to client: " + json);
        writer.println(json);
        return !writer.checkError();
    }

    public void stop() {
        try {
            client.close();
        } catch (IOException exception) {
            LogUtil.severe("Cannot close client socket!");
            exception.printStackTrace();
        }
        this.cancel();
    }
}
