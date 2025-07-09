package me.peterterpe.boatrace;

import com.google.gson.*;
import java.lang.reflect.Type;

public class RaceResultAdapter implements JsonSerializer<RaceResult>, JsonDeserializer<RaceResult> {
        public static final RaceResultAdapter INSTANCE = new RaceResultAdapter();

        @Override
        public JsonElement serialize(RaceResult result, Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", result.getPlayerName());
            obj.addProperty("time", result.getTimeInMs());
            return obj;
        }

        @Override
        public RaceResult deserialize(JsonElement el, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
            JsonObject obj = el.getAsJsonObject();
            String name = obj.get("name").getAsString();
            long time = obj.get("time").getAsLong();
            return new RaceResult(name, time);
        }
    }