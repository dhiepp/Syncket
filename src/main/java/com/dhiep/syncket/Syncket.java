package com.dhiep.syncket;

import com.dhiep.syncket.commands.SyncCommand;
import com.dhiep.syncket.commands.SyncketCommand;
import com.dhiep.syncket.sockets.SyncketManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Syncket extends JavaPlugin {
    private static Syncket instance;

    public static Syncket getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        SyncketManager.load();
        SyncketManager.start();

        this.getCommand("sync").setExecutor(new SyncCommand());
        this.getCommand("syncket").setExecutor(new SyncketCommand());

        this.getCommand("sync").setTabCompleter(new SyncCommand());
        this.getCommand("syncket").setTabCompleter(new SyncketCommand());
    }

    @Override
    public void onDisable() {
        SyncketManager.stop();
    }
}
