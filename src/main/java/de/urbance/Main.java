package de.urbance;

import Commands.Kudmin;
import Commands.Kudo;
import Commands.Kudos;
import Events.OnPlayerJoin;
import GUI.OverviewGUI;
import GUI.ReceivedKudosGUI;
import Utils.*;
import Utils.KudosUtils.KudosExpansion;
import Utils.SQL.SQL;
import Utils.SQL.SQLGetter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Main extends JavaPlugin implements Listener {
    public static boolean oldTableScheme;
    public String prefix;
    public Utils.SQL.SQL SQL;
    public FileConfiguration config;
    public SQLGetter data;
    public boolean isConnected;

    @Override
    public void onEnable() {
        getLogger().info("Successfully launched. Suggestions? Questions? Report a Bug? Visit my discord server! https://discord.gg/hDqPms3MbH");

        setupConfigs();
        checkNewUpdateAndCurrentVersion();
        setupMetrics();
        setupSQL();

        if (data.oldDateFormatUsed()) {
            data.convertOldDateFormatToNewDateFormat();
        }

        WorkaroundManagement workaroundManagement = new WorkaroundManagement();
        workaroundManagement.performMigrationCheck();

        if (WorkaroundManagement.isLegacyConfig) {
            WorkaroundManagement.notifyInstanceAboutLegacyConfigAtPluginStartup();
            return;
        }

        if (WorkaroundManagement.isSQLMigrationNeeded || WorkaroundManagement.isConfigMigrationNeeded) {
            registerListenerAndCommands();
            WorkaroundManagement.notifyInstanceAboutWorkaroundAtPluginStartup();
            return;
        }

        registerListenerAndCommands();
    }

    @Override
    public void onDisable() {
        Utils.SQL.SQL.disconnect();
    }

    public boolean setupSQL() {
        this.SQL = new SQL();
        this.data = new SQLGetter(this);

        try {
            SQL.connect();
        }
        catch (Exception exception) {
            this.isConnected = false;
            getLogger().info("Database is not connected");
            throw exception;
        }
        try {
            if (!Utils.SQL.SQL.getConnection().isClosed()) {
                getLogger().info("Database is connected");
                if (!data.initTables()) return false;
                this.isConnected = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    public void setupConfigs() {
        ConfigManagement.initConfigFiles(this);
        this.config = ConfigManagement.getConfig();
    }

    public void registerListenerAndCommands() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new OnPlayerJoin(), this);
        if (!isConnected) return;

        getCommand("kudmin").setExecutor(new Kudmin());
        getCommand("kudmin").setTabCompleter(new Kudmin());
        pluginManager.registerEvents(new OverviewGUI(), this);
        pluginManager.registerEvents(new ReceivedKudosGUI(), this);
        getCommand("kudos").setExecutor(new Kudos());
        getCommand("kudos").setTabCompleter(new Kudos());
        getCommand("kudo").setExecutor(new Kudo());
        getCommand("kudo").setTabCompleter(new Kudo());
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new KudosExpansion().register();
        }
    }

    private void setupMetrics() {
        new Metrics(this, 16627);
    }

    public void checkNewUpdateAndCurrentVersion() {
        String pluginVersion = getDescription().getVersion();
        if (pluginVersion.contains("PRE")) getLogger().info("You're using a 'PRE' version. Please notice that bugs can occur!");

        if (!config.getBoolean("general-settings.update-notification")) return;
        new UpdateChecker(this, 106036).getVersion(version -> {
            int majorPluginVersion = Integer.parseInt(pluginVersion.substring(0, pluginVersion.indexOf('.')));
            int majorPluginVersionOnSpigot = Integer.parseInt(version.substring(0, version.indexOf('.')));

            if (majorPluginVersion <= 0) {
                getLogger().info("An error occurred when checking a new update");
                return;
            }
            if (majorPluginVersion < majorPluginVersionOnSpigot) {
                getLogger().info("There is a new major version available. Please check the changelogs for breaking changes!");
                return;
            }
            if (pluginVersion.equals(version)) {
                getLogger().info("There is not a new update available.");
            } else {
                getLogger().info("There is a new update available.");
            }
        });
    }
}
