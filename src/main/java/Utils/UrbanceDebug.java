package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class UrbanceDebug {

    public static void sendInfo(String message) {
        Main plugin = Main.getPlugin(Main.class);
        FileConfiguration config = plugin.config;

        if (config.getBoolean("general-settings.debug-mode"))
            Bukkit.getLogger().info("[Kudos Debug] " + message);
    }
}
