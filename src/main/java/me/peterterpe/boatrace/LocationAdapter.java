package me.peterterpe.boatrace;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import java.lang.reflect.Type;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {
        public static final LocationAdapter INSTANCE = new LocationAdapter();

        @Override
        public JsonElement serialize(Location loc, Type type, JsonSerializationContext ctx) {
            JsonObject obj = new JsonObject();
            obj.addProperty("world", loc.getWorld().getName());
            obj.addProperty("x", loc.getX());
            obj.addProperty("y", loc.getY());
            obj.addProperty("z", loc.getZ());
            return obj;
        }

        @Override
        public Location deserialize(JsonElement el, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
            JsonObject obj = el.getAsJsonObject();
            World w = Bukkit.getWorld(obj.get("world").getAsString());
            double x = obj.get("x").getAsDouble();
            double y = obj.get("y").getAsDouble();
            double z = obj.get("z").getAsDouble();
            return new Location(w, x, y, z);
        }
    }