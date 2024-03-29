package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigKey {
    private Main plugin;
    private FileConfiguration guiConfig;
    private FileConfiguration localeConfig;

    public ConfigKey() {
        this.plugin = Main.getPlugin(Main.class);
        this.guiConfig = plugin.guiConfig;
        this.localeConfig = plugin.localeConfig;
    }
    /*
        GUI-Config
    */
    public int guiGeneralRows() {
        String configKey = "general.rows";
        try {
            String size = guiConfig.getString(configKey);
            int sizeAsInteger = Integer.parseInt(size);

            if (sizeAsInteger > 0 && sizeAsInteger < 7) return sizeAsInteger;
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }

    public String leaderboard_player_leaderboard_item_item_name() {
        return guiConfig.getString("leaderboard.player-leaderboard-item.item-name");
    }

    public List<String> leaderboard_player_leaderboard_item_item_lore() {
        return guiConfig.getStringList("leaderboard.player-leaderboard-item.lore");
    }

    public List<String> slot_kudos_leaderboard_lore() {
        return guiConfig.getStringList("slot.kudos-leaderboard.lore");
    }

    public List<String> slot_kudos_leaderboard_lore_no_kudos_exists() {
        return guiConfig.getStringList("slot.kudos-leaderboard.lore-no-kudos-exists");
    }

    /*
        Locale Config
     */
    public String errorSomethingWentWrongPleaseContactServerAdministrator() {
        return localeConfig.getString("error.something-went-wrong-please-contact-server-administrator");
    }
}