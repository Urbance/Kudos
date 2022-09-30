package de.urbance;

import Commands.Kudos;
import Utils.GUI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        // Plugin startup logic
        config.options().copyDefaults(true);
        saveConfig();

        // Register Listeners and Commands
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GUI(), this);
        getCommand("kudos").setExecutor(new Kudos());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getConfigFile() {
        return getConfig();
    }
}
