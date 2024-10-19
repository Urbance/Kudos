package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static Utils.FileManager.copyConfigValuesBetweenTwoConfigs;

public class WorkaroundManagement {
    public static boolean isMigrationNeeded = false;
    private Main plugin;
    private Path guiConfigPath;

    public WorkaroundManagement() {
        this.plugin = Main.getPlugin(Main.class);
        this.guiConfigPath = Path.of(plugin.getDataFolder() + "\\gui.yml");
    }

    public void performMigrationCheck(boolean performWorkaround) {
        if (check500Workaround())
            isMigrationNeeded = true;

        if (isMigrationNeeded && performWorkaround) {
            performWorkarounds();
            isMigrationNeeded = false;
        }

    }

    private void performWorkarounds() {
        if (!isMigrationNeeded)
            return;

        perform500Workaround();
    }

    private boolean check500Workaround() {
        if (!Files.exists(guiConfigPath))
            return false;

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();
        guiConfig.options().copyDefaults(true);

        return guiConfigFileManager.file.exists() && guiConfig.isConfigurationSection("general") && guiConfig.isConfigurationSection("slot");
    }


    public void perform500Workaround() {
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


        // Step 10: delete unused config keys in config.yml
        config.set("general", null);
        config.set("kudo-award.cooldown", null);
        config.set("kudo-award.enable-reasons", null);
        config.set("kudo-award.reason-length", null);
        config.set("kudo-award.no-reason-given", null);


        // Step 11: save configs
        configFileManager.save();
        globalGuiSettingsConfigManager.save();
        guiConfigFileManager.save();
        leaderboardGuiConfigManager.save();
        overviewGuiConfigManager.save();


        // Step 12: delete gui.yml
        if (!guiConfigFileManager.deleteConfigFile())
            Bukkit.getLogger().severe("Can't delete gui.yml. Consider to delete the config file manually.");

    }

    public static String workaroundNeededMessage(boolean consoleMessage) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&7========= &e&lKudos&r &7=========\n");
        stringBuilder.append("\n&7It seems you already had an older version of Kudos installed.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nThere have been critical changes to the plugin. Please execute the command &e/kudmin migration &7to continue using Kudos.");
        stringBuilder.append("\n ");
        stringBuilder.append("\nData loss may occur. A backup of the database and configuration files is recommended.");
        stringBuilder.append("\n&7========= &e&lKudos&r &7=========\n");

        String message = stringBuilder.toString();

        if (consoleMessage) {
            message = "\n " + message.replaceAll("&.", "");
        }

        return message;
    }

    public static boolean notifyWhenWorkaroundIsNeeded(CommandSender sender, boolean pluginStartup) {
        WorkaroundManagement workaroundManagement = new WorkaroundManagement();
        workaroundManagement.performMigrationCheck(false);

        if (isMigrationNeeded) {
            if (pluginStartup) {
                Bukkit.getLogger().severe(WorkaroundManagement.workaroundNeededMessage(true));

                for (Player players : Bukkit.getOnlinePlayers()) {
                    if (players.hasPermission("kudos.admin.*")) players.sendMessage(ChatColor.translateAlternateColorCodes('&', WorkaroundManagement.workaroundNeededMessage(false)));
                }
                return true;
            }

            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', workaroundNeededMessage(false)));
            } else {
                sender.sendMessage(workaroundNeededMessage(true));
            }
            return true;
        }
        return false;
    }
}
