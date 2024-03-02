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

    public ConfigWorkaroundManagement() {
        this.plugin = Main.getPlugin(Main.class);
    }

    public void performWorkarounds() {
        // 4.3.0 Minor Update
        // move received kudos page switcher to general key

        FileManager guiConfigManagerDeprecated = new FileManager("gui.yml", plugin);
        FileConfiguration guiConfigDeprecated = guiConfigManagerDeprecated.getConfig();

        String receivedKudosBackwardsItemNameKeyPath = "received-kudos.backwards-item.item-name";
        String receivedKudosForwardsItemNameKeyPath = "received-kudos.forwards-item.item-name";

        String generalPageSwitcherBackwardsItemNameKeyPath = "general.page-switcher.backwards.item-name";
        String generalPageSwitcherForwardsItemNameKeyPath = "general.page-switcher.forwards.item-name";

        if (guiConfigDeprecated.getString(receivedKudosBackwardsItemNameKeyPath) != null) {
            guiConfigDeprecated.set(generalPageSwitcherBackwardsItemNameKeyPath, guiConfigDeprecated.getString(receivedKudosBackwardsItemNameKeyPath));
            guiConfigDeprecated.set("received-kudos.backwards-item", null);
        }

        if (guiConfigDeprecated.getString(receivedKudosForwardsItemNameKeyPath) != null) {
            guiConfigDeprecated.set(generalPageSwitcherForwardsItemNameKeyPath, guiConfigDeprecated.getString(receivedKudosForwardsItemNameKeyPath));
            guiConfigDeprecated.set("received-kudos.forwards-item", null);
        }

        // refactored papi leaderboard config key values into a separated key
        String oldNotAssignedKudosPapiKeyPath = "slot.kudos-leaderboard.not-assigned-kudos";
        String oldLoreFormatPapiKeyPath = "slot.kudos-leaderboard.lore-format";

        if (guiConfigDeprecated.getString(oldNotAssignedKudosPapiKeyPath) != null) {
            String oldNotAssignedKudosPapiKeyValue = guiConfigDeprecated.getString(oldNotAssignedKudosPapiKeyPath);
            String newNotAssignedKudosPapiPath = "slot.kudos-leaderboard.papi.not-assigned-kudos";

            Bukkit.getLogger().info(oldNotAssignedKudosPapiKeyValue);

            guiConfigDeprecated.set(newNotAssignedKudosPapiPath, oldNotAssignedKudosPapiKeyValue);
            guiConfigDeprecated.set(oldNotAssignedKudosPapiKeyPath, null);
        }

        if (guiConfigDeprecated.getString(oldLoreFormatPapiKeyPath) != null) {
            String oldLoreFormatPapiKeyValue = guiConfigDeprecated.getString("slot.kudos-leaderboard.lore-format");
            String newLoreFormatPapiKeyPath = "slot.kudos-leaderboard.papi.lore-format";

            guiConfigDeprecated.set(newLoreFormatPapiKeyPath, oldLoreFormatPapiKeyValue);
            guiConfigDeprecated.set(oldLoreFormatPapiKeyPath, null);
        }

        // TODO replace with FileManager
        guiConfigManagerDeprecated.save();
        plugin.guiConfig = guiConfigManagerDeprecated.getConfig();

    }
}
