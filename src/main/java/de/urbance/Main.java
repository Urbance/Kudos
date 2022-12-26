package de.urbance;

import Commands.Kudmin;
import Commands.Kudo;
import Commands.Kudos;
import Events.OnPlayerJoin;
import Utils.FileManager;
import Utils.GUI;
import Utils.SQL.SQL;
import Utils.SQL.SQLGetter;
import Utils.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class Main extends JavaPlugin implements Listener {
    public static String prefix;
    public FileConfiguration localeConfig;
    public Utils.SQL.SQL SQL;
    public SQLGetter data;
    public FileConfiguration config = getConfig();
    public FileConfiguration mysqlConfig;
    public FileConfiguration guiConfig;
    public boolean isConnected;

    @Override
    public void onEnable() {
        prefix = config.getString("prefix");

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
            if (config.getBoolean("debug-mode"))
                e.printStackTrace();
        }

        if (SQL.isConnected()) {
            Bukkit.getLogger().info("Database is connected");
            data.createTable();
            this.isConnected = true;
        }
    }

    public void setupConfigs() {
        // setup config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();

        // setup mysql.yml
        FileManager mysqlManager = new FileManager("mysql.yml", this);
        this.mysqlConfig = mysqlManager.getConfig();
        mysqlConfig.options().copyDefaults(true);
        mysqlManager.save();

        // setup messages.yml
        FileManager localeManager = new FileManager("messages.yml", this);
        this.localeConfig = localeManager.getConfig();
        localeConfig.options().copyDefaults(true);
        localeManager.save();

        // setup gui.yml
        FileManager guiManager = new FileManager("gui.yml", this);
        this.guiConfig = guiManager.getConfig();
        guiConfig.options().copyDefaults(true);
        guiManager.save();
    }

    public void reloadConfigs() {
        // reload config.yml
        reloadConfig();
        saveDefaultConfig();

        // reload messages.yml
        FileManager localeManager = new FileManager("messages.yml", this);
        localeManager.reload();
        this.localeConfig = localeManager.getConfig();

        // reload mysql.yml
        FileManager mysqlManager = new FileManager("mysql.yml", this);
        mysqlManager.reload();
        this.mysqlConfig = mysqlManager.getConfig();

        // reload gui.yml
        FileManager guiManager = new FileManager("gui.yml", this);
        guiManager.reload();
        this.guiConfig = guiManager.getConfig();
    }

    public void UpdateChecker() {
        if (!config.getBoolean("update_notification")){
            return;
        }
        new UpdateChecker(this, 106036).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("There is not a new update available.");
            } else {
                getLogger().info("There is a new update available.");
            }
        });
    }

    public boolean workaroundChecker() {
        return config.getString("play-sound-on-kudo-award") != null || config.getString("play-sound-type") != null || config.getString("kudo-award-notification.playsound-on-kudo-award") != null || localeConfig.getString("kudo.player-award-kudo") != null;
    }
}
