package de.urbance;

import Commands.Kudos;
import Utils.GUI;
import Utils.SQL;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Main extends JavaPlugin {

    public Utils.SQL SQL;

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

        // SQL
        this.SQL = new SQL();
        try {
            SQL.connect();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().info("Database not connected");
        }

        if (SQL.isConnected()) {
            Bukkit.getLogger().info("Database is connected");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        SQL.disconnect();
    }

    public FileConfiguration getConfigFile() {
        return getConfig();
    }
}
