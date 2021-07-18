package com.dhiep.syncket.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtil {
    public static JsonObject decode(String message, String... requiredMembers) throws Exception {
        JsonObject json;
        JsonElement element = new JsonParser().parse(message);
        if (!element.isJsonObject()) throw new Exception("Not a json object!");
        json = element.getAsJsonObject();

        for (String member : requiredMembers) {
            if (!json.has(member)) throw new Exception("Missing required member!");
        }
        return json;
    }
}
