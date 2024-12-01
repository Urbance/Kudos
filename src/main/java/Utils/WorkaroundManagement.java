package Utils;

import Utils.KudosUtils.KudosMessage;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static Utils.FileManager.copyConfigValuesBetweenTwoConfigs;

public class WorkaroundManagement {
    public static boolean isConfigMigrationNeeded = false;
    public static boolean isSQLMigrationNeeded = false;
    public static boolean isLegacyConfig = false;
    private Main plugin;
    private Path guiConfigPath;

    public WorkaroundManagement() {
        this.plugin = Main.getPlugin(Main.class);
        this.guiConfigPath = Path.of(plugin.getDataFolder() + "\\gui.yml");
    }

    public void performMigrationCheck() {
        if (isConfigOlderThanMajorVersion400()) {
            isLegacyConfig = true;
            return;
        }
        if (checkSQLMigration()) {
            isSQLMigrationNeeded = true;
        }

        if (check430Workaround() || check500Workaround())
            isConfigMigrationNeeded = true;
    }

    public void performConfigMigration() {
        if (isConfigMigrationNeeded) {
            perform430Workaround();
            perform500Workaround();
            WorkaroundManagement.isConfigMigrationNeeded = false;
        }
    }

    public boolean performSQLMigration() {
        if (isSQLMigrationNeeded) {
            SQLGetter data = new SQLGetter(plugin);

            if (data.migrateOldTableSchemeToNewTableScheme())
                return !data.checkIfKudosTableHasOldTableSchematic();
        }
        return false;
    }

    private boolean isConfigOlderThanMajorVersion400() {
        if (!Files.exists(guiConfigPath))
            return false;

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();
        guiConfig.options().copyDefaults(true);

        return guiConfig.getString("title") != null && guiConfig.getString("enabled") != null && guiConfig.isConfigurationSection("slot.top3");

    }

    private boolean checkSQLMigration() {
        SQLGetter data = new SQLGetter(plugin);
        return isSQLMigrationNeeded = data.checkIfKudosTableHasOldTableSchematic();
    }

    private boolean check500Workaround() {
        if (!Files.exists(guiConfigPath))
            return false;

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();
        guiConfig.options().copyDefaults(true);

        return guiConfigFileManager.file.exists() && guiConfig.isConfigurationSection("general") && guiConfig.isConfigurationSection("slot");
    }


    private void perform500Workaround() {
        if (!check500Workaround())
            return;
        if (!Files.exists(guiConfigPath)) {
            return;
        }

        FileManager configFileManager = new FileManager("config.yml", plugin);
        FileConfiguration config = configFileManager.getConfig();
        config.options().copyDefaults(true);

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();
        guiConfig.options().copyDefaults(true);

        FileManager globalGuiSettingsConfigManager = new FileManager("guis/global-gui-settings.yml", plugin);
        FileConfiguration globalGuiSettingsConfig = globalGuiSettingsConfigManager.getConfig();
        globalGuiSettingsConfig.options().copyDefaults(true);

        FileManager leaderboardGuiConfigManager = new FileManager("guis/leaderboard.yml", plugin);
        FileConfiguration leaderboardGuiConfig = leaderboardGuiConfigManager.getConfig();
        leaderboardGuiConfig.options().copyDefaults(true);

        FileManager overviewGuiConfigManager = new FileManager("guis/overview.yml", plugin);
        FileConfiguration overviewGuiConfig = overviewGuiConfigManager.getConfig();
        overviewGuiConfig.options().copyDefaults(true);

        // create config backup files
        configFileManager.createBackup();
        guiConfigFileManager.createBackup();

        // adjust overview gui items to their default values if received kudos item is not existing
        if (!guiConfig.isConfigurationSection("slot.received-kudos")) {
            guiConfig.set("slot.statistics.item-slot", overviewGuiConfig.get("items.statistics.item-slot"));
            guiConfig.set("slot.received-kudos.item-slot", overviewGuiConfig.get("items.received-kudos.item-slot"));
            guiConfig.set("slot.help.item-slot", overviewGuiConfig.get("items.help.item-slot"));
            guiConfig.set("slot.kudos-leaderboard.item-slot", overviewGuiConfig.get("items.kudos-leaderboard.item-slot"));

            guiConfigFileManager.save();
        }

        // perform workaround
        // Step 1: config.yml - general
        Map<String, String> configGeneralSectionKeyMap = new HashMap<>();
        configGeneralSectionKeyMap.put("general.update-notification", "general-settings.update-notification");
        configGeneralSectionKeyMap.put("general.prefix", "general-settings.prefix");
        configGeneralSectionKeyMap.put("general.use-SQL", "general-settings.use-MySQL");
        configGeneralSectionKeyMap.put("general.console-name", "general-settings.console-name");

        copyConfigValuesBetweenTwoConfigs(configGeneralSectionKeyMap, config, config);

        // Step 2: config.yml - kudo-award
        Map<String, String> configKudoAwardSectionKeyMap = new HashMap<>();

        configKudoAwardSectionKeyMap.put("kudo-award.cooldown", "kudo-award.general-settings.cooldown");
        configKudoAwardSectionKeyMap.put("kudo-award.enable-reasons", "kudo-award.general-settings.enable-reasons");
        configKudoAwardSectionKeyMap.put("kudo-award.no-reason-given", "kudo-award.general-settings.no-reason-given");
        configKudoAwardSectionKeyMap.put("kudo-award.reason-length", "kudo-award.general-settings.reason-length");

        copyConfigValuesBetweenTwoConfigs(configKudoAwardSectionKeyMap, config, config);

        // Step 3: gui.yml refactoring -> global-gui-settings.yml
        Map<String, String> guiConfigGlobalGuiSettingsKeyMap = new HashMap<>();

        guiConfigGlobalGuiSettingsKeyMap.put("general.title", "general-settings.gui-title");
        guiConfigGlobalGuiSettingsKeyMap.put("general.page-switcher.backwards.item-name", "page-switcher.direction.backwards.item-name");
        guiConfigGlobalGuiSettingsKeyMap.put("general.page-switcher.forwards.item-name", "page-switcher.direction.forwards.item-name");
        guiConfigGlobalGuiSettingsKeyMap.put("general.page-switcher.playsound.enabled", "page-switcher.playsound.enabled");
        guiConfigGlobalGuiSettingsKeyMap.put("general.page-switcher.playsound.playsound-type", "page-switcher.playsound.playsound-type");

        copyConfigValuesBetweenTwoConfigs(guiConfigGlobalGuiSettingsKeyMap, guiConfig, globalGuiSettingsConfig);


        // Step 4: gui.yml refactoring -> leaderboard.yml
        Map<String, String> guiConfigLeaderboardKeyMap = new HashMap<>();

        guiConfigLeaderboardKeyMap.put("leaderboard.player-leaderboard-item.item-name", "items.player-leaderboard-item.item-name");
        guiConfigLeaderboardKeyMap.put("leaderboard.player-leaderboard-item.lore", "items.player-leaderboard-item.item-lore");

        copyConfigValuesBetweenTwoConfigs(guiConfigLeaderboardKeyMap, guiConfig, leaderboardGuiConfig);


        // Step 5: gui.yml refactoring general -> overview.yml
        Map<String, String> guiConfigOverviewKeyMap = new HashMap<>();

        guiConfigOverviewKeyMap.put("general.enabled", "general-settings.enabled");
        guiConfigOverviewKeyMap.put("general.rows", "general-settings.rows");

        copyConfigValuesBetweenTwoConfigs(guiConfigOverviewKeyMap, guiConfig, overviewGuiConfig);


        // Step 6: gui.yml refactoring statistics slot -> overview.yml
        Map<String, String> guiConfigStatisticsKeyMap = new HashMap<>();

        guiConfigStatisticsKeyMap.put("slot.statistics.enabled", "items.statistics.enabled");
        guiConfigStatisticsKeyMap.put("slot.statistics.item", "items.statistics.item");
        guiConfigStatisticsKeyMap.put("slot.statistics.item-name", "items.statistics.item-name");
        guiConfigStatisticsKeyMap.put("slot.statistics.item-slot", "items.statistics.item-slot");
        guiConfigStatisticsKeyMap.put("slot.statistics.lore", "items.statistics.item-lore");

        copyConfigValuesBetweenTwoConfigs(guiConfigStatisticsKeyMap, guiConfig, overviewGuiConfig);


        // Step 7: gui.yml refactoring received-kudos slot -> overview.yml
        Map<String, String> guiConfigReceivedKudosKeyMap = new HashMap<>();

        guiConfigReceivedKudosKeyMap.put("slot.received-kudos.enabled", "items.received-kudos.enabled");
        guiConfigReceivedKudosKeyMap.put("slot.received-kudos.item", "items.received-kudos.item");
        guiConfigReceivedKudosKeyMap.put("slot.received-kudos.item-name", "items.received-kudos.item-name");
        guiConfigReceivedKudosKeyMap.put("slot.received-kudos.item-slot", "items.received-kudos.item-slot");
        guiConfigReceivedKudosKeyMap.put("slot.received-kudos.lore", "items.received-kudos.item-lore");
        guiConfigReceivedKudosKeyMap.put("slot.received-kudos.lore-no-received-kudos", "items.received-kudos.item-lore-no-received-kudos");

        copyConfigValuesBetweenTwoConfigs(guiConfigReceivedKudosKeyMap, guiConfig, overviewGuiConfig);


        // Step 8: gui.yml refactoring help slot -> overview.yml
        Map<String, String> guiConfigHelpKeyMap = new HashMap<>();

        guiConfigHelpKeyMap.put("slot.help.enabled", "items.help.enabled");
        guiConfigHelpKeyMap.put("slot.help.item", "items.help.item");
        guiConfigHelpKeyMap.put("slot.help.item-name", "items.help.item-name");
        guiConfigHelpKeyMap.put("slot.help.item-slot", "items.help.item-slot");
        guiConfigHelpKeyMap.put("slot.help.lore", "items.help.item-lore");

        copyConfigValuesBetweenTwoConfigs(guiConfigHelpKeyMap, guiConfig, overviewGuiConfig);


        // Step 9: gui.yml refactoring kudos-leaderboard slot -> overview.yml
        Map<String, String> guiConfigKudosLeaderboardKeyMap = new HashMap<>();

        guiConfigKudosLeaderboardKeyMap.put("slot.kudos-leaderboard.enabled", "items.kudos-leaderboard.enabled");
        guiConfigKudosLeaderboardKeyMap.put("slot.kudos-leaderboard.item", "items.kudos-leaderboard.item");
        guiConfigKudosLeaderboardKeyMap.put("slot.kudos-leaderboard.item-name", "items.kudos-leaderboard.item-name");
        guiConfigKudosLeaderboardKeyMap.put("slot.kudos-leaderboard.item-slot", "items.kudos-leaderboard.item-slot");
        guiConfigKudosLeaderboardKeyMap.put("slot.kudos-leaderboard.lore", "items.kudos-leaderboard.item-lore");
        guiConfigKudosLeaderboardKeyMap.put("slot.kudos-leaderboard.lore-no-kudos-exists", "items.kudos-leaderboard.item-lore-no-kudos-exists");

        copyConfigValuesBetweenTwoConfigs(guiConfigKudosLeaderboardKeyMap, guiConfig, overviewGuiConfig);

        // Step 10: config.yml move playsound settings
        Map<String, String> configPlaysoundKeyMap = new HashMap<>();

        configPlaysoundKeyMap.put("kudo-award.milestones.enable-playsound", "kudo-award.milestones.playsound.enabled");
        configPlaysoundKeyMap.put("kudo-award.milestones.playsound-type", "kudo-award.milestones.playsound.playsound-type");

        copyConfigValuesBetweenTwoConfigs(configPlaysoundKeyMap, config, config);

        // Step 11: delete unused config keys in config.yml
        config.set("general", null);
        config.set("kudo-award.cooldown", null);
        config.set("kudo-award.enable-reasons", null);
        config.set("kudo-award.reason-length", null);
        config.set("kudo-award.no-reason-given", null);
        config.set("kudo-award.milestones.playsound.playsound-type", null);
        config.set("kudo-award.milestones.enable-playsound", null);
        config.set("kudo-award.milestones.playsound-type", null);


        // Step 12: save configs
        configFileManager.save();
        globalGuiSettingsConfigManager.save();
        guiConfigFileManager.save();
        leaderboardGuiConfigManager.save();
        overviewGuiConfigManager.save();


        // Step 13: delete gui.yml
        if (!guiConfigFileManager.deleteConfigFile())
            Bukkit.getLogger().warning("Can't delete gui.yml. Consider to delete the config file manually.");

    }

    private boolean check430Workaround() {
        if (!Files.exists(guiConfigPath))
            return false;

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();
        guiConfig.options().copyDefaults(true);

        return guiConfig.getString("received-kudos.backwards-item.item-name") != null || guiConfig.getString("slot.kudos-leaderboard.not-assigned-kudos") != null;
    }

    private void perform430Workaround() {
        if (!check430Workaround())
            return;

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();

        String receivedKudosBackwardsItemNameKeyPath = "received-kudos.backwards-item.item-name";
        String receivedKudosForwardsItemNameKeyPath = "received-kudos.forwards-item.item-name";

        String generalPageSwitcherBackwardsItemNameKeyPath = "general.page-switcher.backwards.item-name";
        String generalPageSwitcherForwardsItemNameKeyPath = "general.page-switcher.forwards.item-name";

        if (guiConfig.getString(receivedKudosBackwardsItemNameKeyPath) != null) {
            guiConfig.set(generalPageSwitcherBackwardsItemNameKeyPath, guiConfig.getString(receivedKudosBackwardsItemNameKeyPath));
            guiConfig.set("received-kudos.backwards-item", null);
        }

        if (guiConfig.getString(receivedKudosForwardsItemNameKeyPath) != null) {
            guiConfig.set(generalPageSwitcherForwardsItemNameKeyPath, guiConfig.getString(receivedKudosForwardsItemNameKeyPath));
            guiConfig.set("received-kudos.forwards-item", null);
        }

        // refactored papi leaderboard config key values into a separated key
        String oldNotAssignedKudosPapiKeyPath = "slot.kudos-leaderboard.not-assigned-kudos";
        String oldLoreFormatPapiKeyPath = "slot.kudos-leaderboard.lore-format";

        if (guiConfig.getString(oldNotAssignedKudosPapiKeyPath) != null) {
            String oldNotAssignedKudosPapiKeyValue = guiConfig.getString(oldNotAssignedKudosPapiKeyPath);
            String newNotAssignedKudosPapiPath = "slot.kudos-leaderboard.papi.not-assigned-kudos";

            guiConfig.set(newNotAssignedKudosPapiPath, oldNotAssignedKudosPapiKeyValue);
            guiConfig.set(oldNotAssignedKudosPapiKeyPath, null);
        }

        if (guiConfig.getString(oldLoreFormatPapiKeyPath) != null) {
            String oldLoreFormatPapiKeyValue = guiConfig.getString("slot.kudos-leaderboard.lore-format");
            String newLoreFormatPapiKeyPath = "slot.kudos-leaderboard.papi.lore-format";

            guiConfig.set(newLoreFormatPapiKeyPath, oldLoreFormatPapiKeyValue);
            guiConfig.set(oldLoreFormatPapiKeyPath, null);
        }
        
        guiConfigFileManager.save();
    }

    private static String getConfigWorkaroundNeededMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&7========= &e&lKudos&r &7=========\n");
        stringBuilder.append("\n&7It seems you already had an older version of Kudos installed.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nThere have been critical changes to the plugin. Please execute the command &e/kudmin migration &7to continue using Kudos.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nData loss may occur. A backup of the database and configuration files is recommended.");
        stringBuilder.append("\n&7========= &e&lKudos&r &7=========\n");

        return stringBuilder.toString();
    }

    private static String getSQLWorkaroundMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&7========= &e&lKudos&r &7=========\n");
        stringBuilder.append("\n&7It seems you already had an older version of Kudos installed.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nThere have been critical changes to the plugin. Please execute the command &e/kudmin migration &7to continue using Kudos.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nThe statistics on the awarded Kudos will be reset after the migration!");
        stringBuilder.append("\n ");
        stringBuilder.append("\n&7Data loss may occur. A backup of the database is recommended to avoid data loss.");
        stringBuilder.append("\n&7========= &e&lKudos&r &7=========\n");

        return stringBuilder.toString();
    }

    private static String getSQLAndConfigWorkaroundMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&7========= &e&lKudos&r &7=========\n");
        stringBuilder.append("\n&7It seems you already had an older version of Kudos installed.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nThere have been critical changes to the plugin. Please execute the command &e/kudmin migration &7to continue using Kudos.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nThe statistics on the awarded Kudos will be reset after the migration!");
        stringBuilder.append("\n ");
        stringBuilder.append("\nData loss may occur. A backup of the database and configuration files is recommended.");
        stringBuilder.append("\n&7========= &e&lKudos&r &7=========\n");

        return stringBuilder.toString();
    }

    private static String getWorkaroundNeededMessage() {
        String message = "";

        if (isConfigMigrationNeeded && isSQLMigrationNeeded) {
            message = getSQLAndConfigWorkaroundMessage();
        } else if (isConfigMigrationNeeded) {
            message = getConfigWorkaroundNeededMessage();
        } else if (isSQLMigrationNeeded) {
            message = getSQLWorkaroundMessage();
        }

        return message;
    }

    private static String getLegacyConfigMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&7========= &e&lKudos&r &7=========\n");
        stringBuilder.append("\n&7You were using a very old version of Kudos and a lot has changed since then.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nTo continue, stop the server, delete &econfig.yml &7and &egui.yml &7in the Kudos plugins folder and restart the server.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nYou will then be prompted to execute a command to update the database to the correct version.");
        stringBuilder.append("\n&7========= &e&lKudos&r &7=========\n");

        return stringBuilder.toString();
    }


    public static void notifyInstanceAboutWorkaround(CommandSender sender) {
        String message = getWorkaroundNeededMessage();
        if (message.isBlank()) return;

        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(KudosMessage.formatStringForConsole(message));
        }

        if (sender instanceof Player && sender.hasPermission("kudos.admin.*")) {
            if (!sender.hasPermission("kudos.admin.*")) return;
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public static void notifyInstanceAboutWorkaroundAtPluginStartup() {
        String message = getWorkaroundNeededMessage();
        if (message.isBlank()) return;

        Bukkit.getLogger().warning(KudosMessage.formatStringForConsole(message));
        for (Player players : Bukkit.getOnlinePlayers())
            if (players.hasPermission("kudos.admin.*"))
                players.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void notifyInstanceAboutLegacyWorkaround(CommandSender sender) {
        String message = getLegacyConfigMessage();

        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(KudosMessage.formatStringForConsole(message));
        }

        if (sender instanceof Player && sender.hasPermission("kudos.admin.*")) {
            if (!sender.hasPermission("kudos.admin.*")) return;
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public static void notifyInstanceAboutLegacyConfigAtPluginStartup() {
        String message = getLegacyConfigMessage();

        Bukkit.getLogger().warning(KudosMessage.formatStringForConsole(message));
        for (Player players : Bukkit.getOnlinePlayers())
            if (players.hasPermission("kudos.admin.*"))
                players.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
