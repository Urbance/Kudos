package de.urbance;

import Commands.Kudmin;
import Commands.Kudo;
import Commands.Kudos;
import Events.OnPlayerJoin;
import Utils.GUI;
import Utils.LocaleManager;
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
    public FileConfiguration locale;
    public Utils.SQL.SQL SQL;
    public SQLGetter data;

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        getLogger().info("Successfully launched. For plugin support visit my Discord server: https://discord.gg/hDqPms3MbH");

        prefix = config.getString("prefix");
        this.locale = new LocaleManager(this).getConfig();

        setupSQL();
        setupConfigs();

        // Register Listeners and Commands
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new GUI(), this);
        pluginManager.registerEvents(new OnPlayerJoin(), this);
        getCommand("kudos").setExecutor(new Kudos());
        getCommand("kudo").setExecutor(new Kudo());
        getCommand("kudmin").setExecutor(new Kudmin());

        // bStats
        Metrics metrics = new Metrics(this, 16627);
    }

    @Override
    public void onDisable() {
        SQL.disconnect();
    }

    public void setupSQL(){
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

    public void setupConfigs() {
        config.options().copyDefaults(true);
        saveConfig();
    }

    public FileConfiguration getConfigFile() {
        return getConfig();
    }

    public void reloadConfigs() {
        // Reload config
        reloadConfig();
        saveConfig();

        // Reload messages.yml
        LocaleManager localeManager = new LocaleManager(this);
        FileConfiguration locale = localeManager.getConfig();
        localeManager.reloadLocale();
        this.locale = locale;
    }
}
