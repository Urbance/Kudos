package de.urbance;

import Commands.Kudo;
import Commands.Kudos;
import Events.OnPlayerJoin;
import Utils.GUI;
import Utils.SQL.SQL;
import Utils.SQL.SQLGetter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.sql.SQLException;

public final class Main extends JavaPlugin implements Listener {
    public static String prefix;

    public Utils.SQL.SQL SQL;
    public SQLGetter data;

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        config.options().copyDefaults(true);
        saveConfig();

        prefix = config.getString("prefix");

        // Register Listeners and Commands
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GUI(), this);
        pluginManager.registerEvents(new OnPlayerJoin(), this);
        getCommand("kudos").setExecutor(new Kudos());
        getCommand("kudo").setExecutor(new Kudo());

        // SQL Stuff
        this.SQL = new SQL();
        this.data = new SQLGetter(this);

        try {
            SQL.connect();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().info("Database not connected");
        }

        if (SQL.isConnected()) {
            Bukkit.getLogger().info("Database is connected");
            data.createTable();
        }
    }

    @Override
    public void onDisable() {
        SQL.disconnect();
    }

    public FileConfiguration getConfigFile() {
        return getConfig();
    }

}
