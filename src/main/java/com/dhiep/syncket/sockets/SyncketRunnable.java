package com.dhiep.syncket.sockets;

import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.google.gson.JsonElement;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class SyncketRunnable extends BukkitRunnable {
    public abstract boolean send(SendMode mode, ActionType action, String target, JsonElement data);
    public abstract void stop();
}
