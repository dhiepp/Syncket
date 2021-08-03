package com.dhiep.syncket.events;

import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomSyncketEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String source;
    private final JsonElement data;

    public CustomSyncketEvent(String source, JsonElement data) {
        this.source = source;
        this.data = data;
    }

    public String getSource() {
        return source;
    }

    public JsonElement getData() {
        return data;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
