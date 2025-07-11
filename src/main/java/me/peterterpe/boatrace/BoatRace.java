package me.peterterpe.boatrace;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.bukkit.plugin.java.JavaPlugin;

import me.peterterpe.boatrace.listeners.CountdownMoveListener;
import me.peterterpe.boatrace.listeners.RaceListener;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;


public class BoatRace extends JavaPlugin {

    private static BoatRace instance;
    private RaceManager raceManager;
    private BukkitAudiences adventure;

    public static BoatRace getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;  // Ensure getInstance() returns a valid reference
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        // Localization setup
        TranslationStore.StringBased<MessageFormat> store = TranslationStore.messageFormat(Key.key("namespace:value"));
        for (Locale locale : List.of(Locale.US, Locale.CHINA)) {
            ResourceBundle bundle = ResourceBundle.getBundle("boatrace.Bundle", locale, UTF8ResourceBundleControl.get());
            store.registerAll(locale, bundle, true);
        }
        GlobalTranslator.translator().addSource(store);

        // Core race setup
        this.raceManager = new RaceManager();
        getCommand("race").setExecutor(new RaceCommandHandler());
        getServer().getPluginManager().registerEvents(new RaceListener(), this);
        getServer().getPluginManager().registerEvents(new CountdownMoveListener(raceManager), this);
        StorageManager.getInstance().loadAll();
        getLogger().info("Plugin enabled successfully!");
    }

    public RaceManager getRaceManager() {
        return raceManager;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }

    public void announce(Component message) {
        adventure.all().sendMessage(message);
    }
}
