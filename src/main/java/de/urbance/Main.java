package de.urbance;

import Commands.Kudmin;
import Commands.Kudo;
import Commands.Kudos;
import Events.OnPlayerJoin;
import Utils.GUI;
import Utils.LocaleManager;
import Utils.SQL.SQL;
import Utils.SQL.SQLGetter;
import Utils.UpdateChecker;
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
    public FileConfiguration config = getConfig();
    public boolean isConnected;

    @Override
    public void onEnable() {
        prefix = config.getString("prefix");
        this.locale = new LocaleManager(this).getConfig();

        getLogger().info("Successfully launched. Suggestions? Questions? Report a Bug? Visit my discord server! https://discord.gg/hDqPms3MbH");

        setupSQL();
        setupConfigs();
        UpdateChecker();
        registerListenerAndCommands();

        // bStats
        Metrics metrics = new Metrics(this, 16627);
    }

    @Override
    public void onDisable() {
        SQL.disconnect();
    }

    public void registerListenerAndCommands() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new OnPlayerJoin(), this);
        if (!isConnected) {
            return;
        }
        pluginManager.registerEvents(new GUI(), this);
        getCommand("kudos").setExecutor(new Kudos());
        getCommand("kudos").setTabCompleter(new Kudos());
        getCommand("kudo").setExecutor(new Kudo());
        getCommand("kudo").setTabCompleter(new Kudo());
        getCommand("kudmin").setExecutor(new Kudmin());
        getCommand("kudmin").setTabCompleter(new Kudmin());
    }

    public void setupSQL(){
        this.SQL = new SQL();
        this.data = new SQLGetter(this);

        try {
            SQL.connect();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().info("Database not connected");
            this.isConnected = false;
        }

        if (SQL.isConnected()) {
            Bukkit.getLogger().info("Database is connected");
            data.createTable();
            this.isConnected = true;
        }
    }

    public void setupConfigs() {
        config.options().copyDefaults(true);
        saveDefaultConfig();
    }

    public void reloadConfigs() {
        // Reload config
        reloadConfig();
        saveDefaultConfig();

        // Reload messages.yml
        LocaleManager localeManager = new LocaleManager(this);
        FileConfiguration locale = localeManager.getConfig();
        localeManager.reloadLocale();
        this.locale = locale;
    }

    public void UpdateChecker() {
        new UpdateChecker(this, 106036).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("There is not a new update available.");
            } else {
                getLogger().info("There is a new update available.");
            }
        });
    }
}
