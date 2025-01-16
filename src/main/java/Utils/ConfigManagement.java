package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigManagement {
    private static final HashMap<String, String> configPathMapping = new HashMap<>();
    private static HashMap<String, FileConfiguration> configFiles = new HashMap<>();
    public String fileName;
    public String fullFileName;
    public FileConfiguration fileConfiguration;
    public Main plugin;
    public File file;

    public ConfigManagement(String fileName, Main plugin) {
        this.fileName = fileName;
        this.fullFileName = configPathMapping.get(fileName);
        this.fileConfiguration = ConfigManagement.configFiles.get(fileName);
        this.plugin = plugin;

        saveDefaultConfig();
    }

    public static void initConfigFiles(Main plugin) {
        ConfigManagement.configPathMapping.put("config.yml", "config.yml");
        ConfigManagement.configPathMapping.put("gui.yml", "gui.yml"); // Deprecated configuration file
        ConfigManagement.configPathMapping.put("mysql.yml", "mysql.yml");
        ConfigManagement.configPathMapping.put("messages.yml", "messages.yml");
        ConfigManagement.configPathMapping.put("overview.yml", "guis" + File.separator + "overview.yml");
        ConfigManagement.configPathMapping.put("global-gui-settings.yml", "guis" + File.separator + "global-gui-settings.yml");
        ConfigManagement.configPathMapping.put("leaderboard.yml", "guis" + File.separator + "leaderboard.yml");
        ConfigManagement.configPathMapping.put("received-kudos.yml", "guis" + File.separator + "received-kudos.yml");

        reloadAllConfigs(plugin);
    }

    public static FileConfiguration getConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("config.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static FileConfiguration getMySQLConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("mysql.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static FileConfiguration getLocalesConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("messages.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static FileConfiguration getOverviewGuiConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("overview.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static FileConfiguration getGlobalGuiSettingsConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("global-gui-settings.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static FileConfiguration getLeaderboardGuiConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("leaderboard.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static FileConfiguration getReceivedKudosGuiConfig() {
        Main plugin = Main.getPlugin(Main.class);
        ConfigManagement configManagement = new ConfigManagement("received-kudos.yml", plugin);

        return configManagement.getFileConfiguration();
    }

    public static void reloadAllConfigs(Main plugin) {
        for (String configFileName : configPathMapping.keySet()) {
            ConfigManagement configManagement = new ConfigManagement(configFileName, plugin);
            configManagement.reload();

            if (configFileName.equals("config.yml")) {
                FileConfiguration config = configManagement.getFileConfiguration();
                plugin.prefix = config.getString("general-settings.prefix");
                plugin.config = config;
            }
        }
    }

    public FileConfiguration getFileConfiguration() {
        if (this.fileConfiguration == null) {
            reload();
        }
        return this.fileConfiguration;
    }

    public void reload() {
        if (this.fileConfiguration == null)
            this.file = new File(this.plugin.getDataFolder(), fullFileName);
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);

        InputStream defaultStream = this.plugin.getResource(fullFileName);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            this.fileConfiguration.setDefaults(defaultConfig);
        }

        ConfigManagement.configFiles.put(fileName, fileConfiguration);
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
            this.file = new File(this.plugin.getDataFolder(), fullFileName);

        if (!this.file.exists()) {
            this.plugin.saveResource(fullFileName, false);
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
