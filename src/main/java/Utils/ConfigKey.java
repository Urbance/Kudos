package Utils;

import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;

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

    /*
        Locale Config
     */
    public String errorSomethingWentWrongPleaseContactServerAdministrator() {
        return localeConfig.getString("error.something-went-wrong-please-contact-server-administrator");
    }
}