package com.dhiep.syncket.commands;

import com.dhiep.syncket.models.ActionType;
import com.dhiep.syncket.models.SendMode;
import com.dhiep.syncket.sockets.SyncketManager;
import com.dhiep.syncket.utils.MessageUtil;
import com.google.gson.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public class SyncCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("syncket.use")) {
            MessageUtil.send(sender, "&cBạn không có quyền sử dụng lệnh này.");
            return true;
        }
        if (args.length < 3) {
            MessageUtil.send(sender, "&cUsage: &7/sync <mode> <action> [target] <data>");
            return true;
        }

        SendMode mode;
        ActionType action;
        String target;
        String raw;
        try {
            mode = SendMode.valueOf(args[0].toUpperCase(Locale.ROOT));
            action = ActionType.valueOf(args[1].toUpperCase(Locale.ROOT));
            if (mode == SendMode.SPECIFIC) {
                if (args.length < 4) throw new IllegalArgumentException("Missing target");
                target = args[2];
                raw = Arrays.stream(args).skip(3).collect(Collectors.joining(" "));
            } else {
                target = null;
                raw = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
            }
        } catch (IllegalArgumentException exception) {
            MessageUtil.send(sender, "&cUsage: &7/sync <mode> <action> [target] <data>");
            return true;
        }

        JsonElement data;
        try {
            data = new JsonParser().parse(raw);
        } catch (JsonParseException exception) {
            data = new JsonPrimitive(raw);
        }

        if (SyncketManager.send(mode, action, target, data)) {
            MessageUtil.send(sender, "&aSuccessfully sent your syncket package!");
        } else {
            MessageUtil.send(sender, "&cCannot send your syncket package!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("syncket.use")) {
            return null;
        }

        if (args.length == 1) {
            return Arrays.stream(SendMode.values()).map(Enum::toString)
                    .filter(mode -> mode.startsWith(args[0])).collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Arrays.stream(ActionType.values()).map(Enum::toString)
                    .filter(action -> action.startsWith(args[1])).collect(Collectors.toList());
        }

        return null;
    }
}
