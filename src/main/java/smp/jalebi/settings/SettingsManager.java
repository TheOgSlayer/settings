package smp.jalebi.settings;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/** true = ANYONE / ON (the default), false = NO ONE / OFF */
public class SettingsManager {
    private final JalebiSettings plugin;
    private final File file;
    private final YamlConfiguration data;

    public SettingsManager(JalebiSettings plugin) {
        this.plugin = plugin;
        plugin.getDataFolder().mkdirs();
        this.file = new File(plugin.getDataFolder(), "settings.yml");
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    private String path(UUID id, Setting s) {
        return "players." + id + "." + s.path;
    }

    public boolean get(UUID id, Setting s) {
        return data.getBoolean(path(id, s), true);
    }

    public boolean toggle(UUID id, Setting s) {
        boolean now = !get(id, s);
        data.set(path(id, s), now);
        save();
        return now;
    }

    private void save() {
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save settings.yml: " + e.getMessage());
        }
    }
}
