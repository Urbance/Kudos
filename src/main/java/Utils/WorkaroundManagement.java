package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

public class WorkaroundManagement {

    public boolean perform500Workaround() {
        Main plugin = Main.getPlugin(Main.class);


        FileManager configFileManager = new FileManager("config.yml", plugin);
        FileConfiguration config = configFileManager.getConfig();

        FileManager guiConfigFileManager = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfig = guiConfigFileManager.getConfig();

        FileManager globalGuiSettingsConfigManager = new FileManager("guis/global-gui-settings.yml", plugin);
        FileConfiguration globalGuiSettingsConfig = globalGuiSettingsConfigManager.getConfig();

        FileManager leaderboardGuiConfigManager = new FileManager("guis/leaderboard.yml", plugin);
        FileConfiguration leaderboardGuiConfig = globalGuiSettingsConfigManager.getConfig();

        FileManager overviewGuiConfigManager = new FileManager("guis/overview.yml", plugin);
        FileConfiguration overviewGuiConfig = overviewGuiConfigManager.getConfig();

        /*
        > TODO, NOTES
        Work with @NotNull
        check messages.yml

         */

        // check

        // TODO: Step: Create backup from config.yml and from gui.yml

        // perform workaround
        // Step 1: config.yml - general
        Bukkit.getLogger().info("DBEUG - Perform Workaround");

        String oldConfigUpdateNotification = config.getString("general.update-notification");
        String oldConfigPrefix = config.getString("general.prefix");
        String oldConfigUseSQL = config.getString("general.use-SQL");
        String oldConsoleName = config.getString("general.console-name");

        config.set("general-settings.update-notification", oldConfigUpdateNotification);
        config.set("general-settings.prefix", oldConfigPrefix);
        config.set("general-settings.use-MySQL", oldConfigUseSQL);
        config.set("general-settings.console-name", oldConsoleName);

        config.set("general.update-notification", null);
        config.set("general.prefix", null);
        config.set("general.use-SQL", null);
        config.set("general.console-name", null);
        config.set("general", null);


        // Step 2: config.yml - kudo-award
        String oldConfigCooldown = config.getString("kudo-award.cooldown");
        String oldConfigEnableReasons = config.getString("kudo-award.enable-reasons");
        String oldConfigNoReasonGiven = config.getString("kudo-award.no-reason-given");
        String oldConfigReasonLength = config.getString("kudo-award.reason-length");

        config.set("kudo-award.general-settings.cooldown", oldConfigCooldown);
        config.set("kudo-award.general-settings.enable-reasons", oldConfigEnableReasons);
        config.set("kudo-award.general-settings.no-reason-given", oldConfigNoReasonGiven);
        config.set("kudo-award.general-settings.reason-length", oldConfigReasonLength);

        config.set("kudo-award.cooldown", null);
        config.set("kudo-award.enable-reasons", null);
        config.set("kudo-award.no-reason-given", null);
        config.set("kudo-award.reason-length", null);


        // Step 3: gui.yml refactoring -> global-gui-settings.yml
        String oldGuiTitle = guiConfig.getString("general.title");
        String oldGuiPageSwitcherBackwardsItemName = guiConfig.getString("general.page-switcher.backwards.item-name");
        String oldGuiPageSwitcherForwardsItemName = guiConfig.getString("general.page-switcher.forwards.item-name");
        String oldGuiPageSwitcherPlaysoundEnabled = guiConfig.getString("general.page-switcher.playsound.enabled");
        String oldGuiPageSwitcherPlaysoundType = guiConfig.getString("general.page-switcher.playsound.playsound-type");

        globalGuiSettingsConfig.set("general-settings.gui-title", oldGuiTitle);
        globalGuiSettingsConfig.set("page-switcher.playsound.enabled", oldGuiPageSwitcherPlaysoundEnabled);
        globalGuiSettingsConfig.set("page-switcher.playsound.playsound-type", oldGuiPageSwitcherPlaysoundType);
        globalGuiSettingsConfig.set("page-switcher.direction.backwards.item-name", oldGuiPageSwitcherBackwardsItemName);
        globalGuiSettingsConfig.set("page-switcher.direction.forwards.item-name", oldGuiPageSwitcherForwardsItemName);

        guiConfig.set("general", null);


        // TODO Add and overthink Papi Settings

        // Step 4: gui.yml refactoring -> leaderboard.yml
        String oldGuiItemsPlayerLeaderboardItemItemName = guiConfig.getString("leaderboard.player-leaderboard-item.item-name");
        List<String> oldGuiItemsPlayerLeaderboardItemItemLore = guiConfig.getStringList("leaderboard.player-leaderboard-item.lore");

        leaderboardGuiConfig.set("items.player-leaderboard-item.item-name", oldGuiItemsPlayerLeaderboardItemItemName);
        leaderboardGuiConfig.set("items.player-leaderboard-item.item-lore", oldGuiItemsPlayerLeaderboardItemItemLore);


        // Step 5: gui.yml refactoring statistics slot -> overview.yml
        String oldGuiEnabled = guiConfig.getString("general.enabled");
        String oldGuiRows = guiConfig.getString("general.rows");

        overviewGuiConfig.set("general-settings.enabled", oldGuiEnabled);
        overviewGuiConfig.set("general-settings.rows", oldGuiRows);

        String oldGuiStatisticsEnabled = guiConfig.getString("slot.statistics.enabled");
        String oldGuiStatisticsItem = guiConfig.getString("slot.statistics.item");
        String oldGuiStatisticsItemName = guiConfig.getString("slot.statistics.item-name");
        String oldGuiStatisticsItemSlot = guiConfig.getString("slot.statistics.item-slot");
        List<String> oldGuiStatisticsItemLore = guiConfig.getStringList("slot.statistics.lore");

        overviewGuiConfig.set("items.statistics.enabled", oldGuiStatisticsEnabled);
        overviewGuiConfig.set("items.statistics.item", oldGuiStatisticsItem);
        overviewGuiConfig.set("items.statistics.item-name", oldGuiStatisticsItemName);
        overviewGuiConfig.set("items.statistics.item-slot", oldGuiStatisticsItemSlot);
        overviewGuiConfig.set("items.statistics.item-lore", oldGuiStatisticsItemLore);

        String oldGuiReceivedKudosEnabled = guiConfig.getString("slot.received-kudos.enabled");
        String oldGuiReceivedKudosItem = guiConfig.getString("slot.received-kudos.item");
        String oldGuiReceivedKudosItemName = guiConfig.getString("slot.received-kudos.item-name");
        String oldGuiReceivedKudosItemSlot = guiConfig.getString("slot.received-kudos.item-slot");
        List<String> oldGuiReceivedKudosItemLore = guiConfig.getStringList("slot.received-kudos.lore");
        List<String> oldGuiReceivedKudosLoreNoReceivedKudos = guiConfig.getStringList("slot.received-kudos.lore-no-received-kudos");

        // TODO move values


        // remove gui config file


        configFileManager.save();
        globalGuiSettingsConfigManager.save();
        guiConfigFileManager.save();
        leaderboardGuiConfigManager.save();

        // check if workaround was successfully (assert object?)
        return false;
    }
}
