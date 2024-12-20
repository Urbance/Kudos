package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class FileManager {
    public String fileName;
    public FileConfiguration fileConfiguration;
    public Main plugin;
    public File file;

    public FileManager(String fileName, Main plugin) {
        this.fileName = fileName;
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public FileConfiguration getConfig() {
        if (this.fileConfiguration == null)
            reload();
        return this.fileConfiguration;
    }

    public void reload() {
        if (this.fileConfiguration == null)
            this.file = new File(this.plugin.getDataFolder(), fileName);
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);

        InputStream defaultStream = this.plugin.getResource(fileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.fileConfiguration.setDefaults(defaultConfig);
        }

        switch (fileName) {
            case "config.yml" -> {
                plugin.config = fileConfiguration;
                plugin.prefix = fileConfiguration.getString("general-settings.prefix");
            }
            case "messages.yml" -> plugin.localeConfig = fileConfiguration;
            case "mysql.yml" -> plugin.mysqlConfig = fileConfiguration;
            case "guis/received-kudos.yml" -> plugin.receivedKudosConfig = fileConfiguration;
            case "guis/leaderboard.yml" -> plugin.leaderboardConfig = fileConfiguration;
            case "guis/overview.yml" -> plugin.overviewConfig = fileConfiguration;
            case "guis/global-gui-settings.yml" -> plugin.globalGuiSettingsConfig = fileConfiguration;
        }
    }

    public void save() {
        if (this.file == null)
            saveDefaultConfig();
        if (this.fileConfiguration == null)
            this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDefaultConfig() {
        if (this.file == null)
            this.file = new File(this.plugin.getDataFolder(), fileName);

        if (!this.file.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }

    public boolean deleteConfigFile() {
        return file.delete();
    }

    public boolean createBackup() {
        String fileNameWithoutFileExtension = fileName.substring(0, fileName.indexOf("."));

        Path sourcePath = file.toPath();
        Path targetPath = file.getParentFile().toPath().resolve(fileNameWithoutFileExtension + "-backup.yml.");

        int backupFileCounter = 2;

        while (Files.exists(targetPath)) {
            targetPath = file.getParentFile().toPath().resolve(fileNameWithoutFileExtension + "-backup-" + backupFileCounter + ".yml");
            backupFileCounter++;
        }

        try {
            Files.copy(sourcePath, targetPath);
            return true;
        } catch (IOException e) {
            Bukkit.getLogger().severe("Cannot create backup file from " + fileName + ". " + e.getMessage());
            return false;
        }
    }

    public static void copyConfigValuesBetweenTwoConfigs(Map<String, String> configKeys, FileConfiguration fromConfig, FileConfiguration toConfig) {
        for (String oldConfigKey : fromConfig.getKeys(true)) {
            if (configKeys.containsKey(oldConfigKey)) {
                String newConfigKey = configKeys.get(oldConfigKey);
                Object configKeyValue = fromConfig.get(oldConfigKey);
                toConfig.set(newConfigKey, configKeyValue);
            }
        }
    }
}
