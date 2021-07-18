package com.dhiep.syncket.commands;

import com.dhiep.syncket.sockets.SyncketManager;
import com.dhiep.syncket.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;

public class SyncketCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("syncket.use")) {
            MessageUtil.send(sender, "&cBạn không có quyền sử dụng lệnh này.");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                SyncketManager.stop();
                SyncketManager.load();
                SyncketManager.start();
                MessageUtil.send(sender, "&e&lSyncket &areloaded!");
                return true;
            }

            if (args[0].equalsIgnoreCase("start")) {
                SyncketManager.start();
                MessageUtil.send(sender, "&e&lSyncket &astarted!");
            }

            if (args[0].equalsIgnoreCase("stop")) {
                SyncketManager.stop();
                MessageUtil.send(sender, "&e&lSyncket &astopped!");
            }
        }

        MessageUtil.send(sender, "&e&lSyncket &d(" + SyncketManager.getType() + ") &7made by &cdhiep\n" +
                "&a/syncket reload&f - Reload config\n" +
                "&a/syncket start&f - Start task\n" +
                "&a/syncket stop&f - stop task");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("syncket.admin")) {
            return null;
        }
        if (args.length == 1) {
            return Arrays.asList("reload", "start", "stop");
        }
        return null;
    }
}
