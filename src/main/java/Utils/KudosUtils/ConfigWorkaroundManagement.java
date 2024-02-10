package Utils.KudosUtils;/*
    The class is called every time the plugin is started.
    It checks whether there are any outdated config keys.
    If keys have been replaced, the values are copied and inserted in the correct place.
 */

import Utils.FileManager;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigWorkaroundManagement {
    private Main plugin;
    private FileManager guiConfigManager;
    private FileConfiguration guiConfig;

    public ConfigWorkaroundManagement() {
        this.plugin = Main.getPlugin(Main.class);
        this.guiConfigManager = new FileManager("gui.yml", plugin);
        this.guiConfig = guiConfigManager.getConfig();
    }

    public void performWorkarounds() {
        // 4.3.0 Minor Update
        // move received kudos page switcher to general key
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

            Bukkit.getLogger().info(oldNotAssignedKudosPapiKeyValue);

            guiConfig.set(newNotAssignedKudosPapiPath, oldNotAssignedKudosPapiKeyValue);
            guiConfig.set(oldNotAssignedKudosPapiKeyPath, null);
        }

        if (guiConfig.getString(oldLoreFormatPapiKeyPath) != null) {
            String oldLoreFormatPapiKeyValue = guiConfig.getString("slot.kudos-leaderboard.lore-format");
            String newLoreFormatPapiKeyPath = "slot.kudos-leaderboard.papi.lore-format";

            guiConfig.set(newLoreFormatPapiKeyPath, oldLoreFormatPapiKeyValue);
            guiConfig.set(oldLoreFormatPapiKeyPath, null);
        }

        // TODO replace with FileManager
        guiConfigManager.save();
        plugin.guiConfig = guiConfigManager.getConfig();
    }
}
