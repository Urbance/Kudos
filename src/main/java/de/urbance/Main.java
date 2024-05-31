package de.urbance;

import Commands.Kudmin;
import Commands.Kudo;
import Commands.Kudos;
import Events.OnPlayerJoin;
import GUI.ReceivedKudosGUI;
import Utils.ConfigKey;
import Utils.CooldownManager;
import Utils.FileManager;
import Utils.KudosUtils.KudosExpansion;
import GUI.KudosGUI;
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
    public final CooldownManager cooldownManager = new CooldownManager();
    public static boolean oldTableScheme;
    public String prefix;
    public FileConfiguration localeConfig;
    public Utils.SQL.SQL SQL;
    public SQLGetter data;
    public FileConfiguration config;
    public FileConfiguration mysqlConfig;
    public FileConfiguration overviewConfig;
    public FileConfiguration receivedKudosConfig;
    public FileConfiguration leaderboardConfig;
    public FileConfiguration globalGuiSettingsConfig;
    public ConfigKey configKey;
    public boolean isConnected;

    @Override
    public void onEnable() {
        this.config = getConfig();
        this.prefix = config.getString("general.prefix");

        getLogger().info("Successfully launched. Suggestions? Questions? Report a Bug? Visit my discord server! https://discord.gg/hDqPms3MbH");

        checkNewUpdateAndCurrentVersion();
        setupMetrics();
        setupConfigs();

        try {
            setupSQL();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        registerListenerAndCommands();
    }

    @Override
    public void onDisable() {
        Utils.SQL.SQL.disconnect();
    }

    public boolean setupSQL() throws SQLException {
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
        if (!Utils.SQL.SQL.getConnection().isClosed()) {
            getLogger().info("Database is connected");
            if (!data.initTables()) return false;
            this.isConnected = true;
        }

        oldTableScheme = data.checkIfKudosTableHasOldTableSchematic();
        if (oldTableScheme) getLogger().warning("Data migration is required. Please create a backup from the database. Perform /kudmin migrate and restart the server. The statistics of how many Kudos a player has awarded will be reset!");

        return true;
    }

    public void registerListenerAndCommands() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new OnPlayerJoin(), this);
        if (!isConnected) return;
        getCommand("kudmin").setExecutor(new Kudmin());
        if (oldTableScheme) return;
        getCommand("kudmin").setTabCompleter(new Kudmin());
        pluginManager.registerEvents(new KudosGUI(), this);
        pluginManager.registerEvents(new ReceivedKudosGUI(), this);
        getCommand("kudos").setExecutor(new Kudos());
        getCommand("kudos").setTabCompleter(new Kudos());
        getCommand("kudo").setExecutor(new Kudo());
        getCommand("kudo").setTabCompleter(new Kudo());
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new KudosExpansion().register();
        }
    }

    public void setupConfigs() {
        // setup config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();
        this.config = getConfig();

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
        FileManager guiManager = new FileManager("guis/overview.yml", this);
        this.overviewConfig = guiManager.getConfig();
        overviewConfig.options().copyDefaults(true);
        guiManager.save();

        // setup received-kudos.yml
        FileManager receivedKudosManager = new FileManager("guis/received-kudos.yml", this);
        this.receivedKudosConfig = receivedKudosManager.getConfig();
        receivedKudosConfig.options().copyDefaults(true);
        receivedKudosManager.save();

        // setup leaderboard.yml
        FileManager leaderboardManager = new FileManager("guis/leaderboard.yml", this);
        this.leaderboardConfig = leaderboardManager.getConfig();
        leaderboardConfig.options().copyDefaults(true);
        leaderboardManager.save();

        // setup global-gui-settings.yml
        FileManager globalGuiSettingsManager = new FileManager("guis/global-gui-settings.yml", this);
        this.globalGuiSettingsConfig = globalGuiSettingsManager.getConfig();
        globalGuiSettingsConfig.options().copyDefaults(true);
        globalGuiSettingsManager.save();

        this.configKey = new ConfigKey();
    }

    private void setupMetrics() {
        new Metrics(this, 16627);
    }

    public void checkNewUpdateAndCurrentVersion() {
        String pluginVersion = getDescription().getVersion();
        if (pluginVersion.contains("PRE")) getLogger().info("You're using a 'PRE' version. Please notice that bugs can occur!");

        if (!config.getBoolean("general.update-notification")) return;
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
