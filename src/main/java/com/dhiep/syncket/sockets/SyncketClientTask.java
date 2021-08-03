package com.dhiep.syncket.sockets;

import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.dhiep.syncket.utils.JsonUtil;
import com.dhiep.syncket.utils.LogUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SyncketClientTask extends SyncketRunnable {
    private Socket client;
    private boolean connect;
    private Scanner scanner;
    private PrintWriter writer;

    public SyncketClientTask(Socket client) {
        this.client = client;
        this.connect = true;
    }

    @Override
    public void run() {
        while (connect) {
            try {
                client = new Socket();
                LogUtil.debug("Connecting to syncket server!");
                client.connect(new InetSocketAddress(SyncketManager.getIp(), SyncketManager.getPort()), SyncketManager.getPulse());
                LogUtil.info("Connected to syncket server!");

                scanner = new Scanner(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8));

                authorize();
                while (scanner.hasNext()) {
                    String message = scanner.nextLine();
                    LogUtil.debug("Received message from server:" + message);

                    JsonObject json;
                    try {
                        json = JsonUtil.decode(message, "action", "data");
                    } catch (Exception exception) {
                        LogUtil.warn("Received invalid data from server");
                        LogUtil.warn(exception.getMessage());
                        continue;
                    }

                    try {
                        ActionType action = ActionType.valueOf(json.get("action").getAsString());
                        JsonElement data = json.get("data");

                        SyncketManager.execute(action, data);
                    } catch (Exception exception) {
                        LogUtil.warn("Received malformed data from server: " + exception.getMessage());
                    }
                }
            } catch (IOException exception) {
                LogUtil.warn("Cannot connect to syncket server!");
            }

            try {
                client.close();
                LogUtil.info("Connection to syncket server closed!");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void authorize() {
        JsonObject json = new JsonObject();
        json.addProperty("password", SyncketManager.getPassword());
        json.addProperty("identifier", SyncketManager.getIdentifier());

        LogUtil.debug("Sending authorization packet");
        writer.println(json);
        writer.flush();
    }

    @Override
    public boolean send(SendMode mode, ActionType action, String target, JsonElement data) {
        JsonObject json = new JsonObject();
        json.addProperty("mode", mode.toString());
        json.addProperty("action", action.toString());
        if (mode == SendMode.OTHERS) {
            target = SyncketManager.getIdentifier();
        }
        json.addProperty("target", target);
        json.add("data", data);

        LogUtil.debug("Sending packet to server: " + json);
        writer.println(json);
        return !writer.checkError();
    }

    @Override
    public void stop() {
        connect = false;
        try {
            client.close();
        } catch (IOException exception) {
            LogUtil.severe("Cannot close client socket!");
            exception.printStackTrace();
        }
        this.cancel();
    }
}
