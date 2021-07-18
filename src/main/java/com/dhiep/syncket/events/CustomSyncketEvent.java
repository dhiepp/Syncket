package com.dhiep.syncket.events;

import com.google.gson.JsonElement;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomSyncketEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final String identifier;
    private final JsonElement data;

    public CustomSyncketEvent(String identifier, JsonElement data) {
        this.identifier = identifier;
        this.data = data;
    }

    public String getIdentifier() {
        return identifier;
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
