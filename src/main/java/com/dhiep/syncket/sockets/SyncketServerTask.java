package com.dhiep.syncket.sockets;

import com.dhiep.syncket.Syncket;
import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.dhiep.syncket.utils.LogUtil;
import com.google.gson.JsonElement;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class SyncketServerTask extends SyncketRunnable {
    private final List<SyncketServerThreadTask> connectedTasks = new ArrayList<>();
    private ServerSocket server;
    private boolean listen;

    SyncketServerTask() {
        this.listen = true;
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(SyncketManager.getIp(), SyncketManager.getPort()));
        } catch (IOException exception) {
            LogUtil.severe("Cannot open server socket!");
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (listen) {
            try {
                Socket client = server.accept();
                LogUtil.debug("Accepted client: " + client);

                SyncketServerThreadTask task = new SyncketServerThreadTask(this, client);
                task.runTaskAsynchronously(Syncket.getInstance());
                connectedTasks.add(task);
            } catch (SocketException exception) {
                LogUtil.debug("Server Socket closed");
                break;
            } catch (IOException exception) {
                LogUtil.severe("Cannot accept a client!");
                exception.printStackTrace();
            }
        }
    }

    @Override
    public boolean send(SendMode mode, ActionType action, String target, JsonElement data) {
        return send(null, mode, action, target, data);
    }

    public boolean send(String source, SendMode mode, ActionType action, String target, JsonElement data) {
        if (source == null) source = SyncketManager.getIdentifier();
        switch (mode) {
            case SERVER:
                SyncketManager.execute(source, action, data);
                return true;
            case CLIENTS:
                for (SyncketServerThreadTask task : connectedTasks) {
                    task.send(source, action, data);
                }
                return true;
            case GROUP:
                boolean sent1 = false;
                if (SyncketManager.getGroup().equalsIgnoreCase(target)) {
                    SyncketManager.execute(source, action, data);
                    sent1 = true;
                }
                for (SyncketServerThreadTask task : connectedTasks) {
                    if (task.getGroup().equalsIgnoreCase(target)) {
                        task.send(source, action, data);
                        sent1 = true;
                    }
                }
                return sent1;
            case SPECIFIC:
                if (SyncketManager.getIdentifier().equalsIgnoreCase(target)) {
                    SyncketManager.execute(source, action, data);
                    return true;
                }
                boolean sent2 = false;
                for (SyncketServerThreadTask task : connectedTasks) {
                    if (task.getIdentifier().equalsIgnoreCase(target)) {
                        task.send(source, action, data);
                        sent2 = true;
                    }
                }
                return sent2;
            case ALL:
                SyncketManager.execute(source, action, data);
                for (SyncketServerThreadTask task : connectedTasks) {
                    task.send(source, action, data);
                }
                return true;
            case OTHERS:
                if (target != null) {
                    SyncketManager.execute(source, action, data);
                }
                for (SyncketServerThreadTask task : connectedTasks) {
                    if (!task.getIdentifier().equalsIgnoreCase(target)) {
                        task.send(source, action, data);
                    }
                }
                return true;
        }
        return false;
    }

    public void remove(SyncketServerThreadTask task) {
        connectedTasks.remove(task);
    }

    @Override
    public void stop() {
        listen = false;
        try {
            server.close();
            for (SyncketServerThreadTask task : connectedTasks) {
                task.stop();
            }
        } catch (IOException exception) {
            LogUtil.severe("Cannot close server socket!");
            exception.printStackTrace();
        }
        this.cancel();
    }
}
