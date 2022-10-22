package Utils;

import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class LocaleManager {
    private Main plugin;
    private FileConfiguration config;
    private FileConfiguration localeConfig;
    private File localeFile;

    public LocaleManager(Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        saveDefaultConfig();
    }

    public void reloadLocale() {
        if (this.localeConfig == null)
            this.localeFile = new File(this.plugin.getDataFolder(), "messages.yml");
        this.localeConfig = YamlConfiguration.loadConfiguration(this.localeFile);

        InputStream defaultStream = this.plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.localeConfig.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (this.localeConfig == null)
            reloadLocale();

        return this.localeConfig;
    }

    public void saveConfig() {
        if (this.localeConfig == null || this.localeFile == null) {
            return;
        }

        try {
            this.getConfig().save(this.localeFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.localeFile, e);
        }
    }

    public void saveDefaultConfig() {
        if (this.localeFile == null)
            this.localeFile = new File(this.plugin.getDataFolder(), "messages.yml");

        if (!this.localeFile.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }
    }
}


