package Utils.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQL {
    private static HikariConfig hikariConfig = new HikariConfig();
    private static HikariDataSource hikariDataSource;
    private FileConfiguration mysqlConfig;
    private FileConfiguration config;
    private Main plugin;
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;
    private String useSSL;

    public SQL() {
        this.plugin = Main.getPlugin(Main.class);
        this.config = plugin.getConfig();
        this.mysqlConfig = plugin.mysqlConfig;
        this.host = mysqlConfig.getString("hostname");
        this.port = mysqlConfig.getString("port");
        this.database = mysqlConfig.getString("database");
        this.username = mysqlConfig.getString("username");
        this.password = mysqlConfig.getString("password");
        this.useSSL = mysqlConfig.getString("useSSL");
    }

    public void connect() {
        if (config.getBoolean("general.use-SQL")) {
            hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?&useSSL=%s", host, port, database, useSSL));
            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            hikariConfig.addDataSourceProperty("cachePrepStmts", true);
            hikariConfig.addDataSourceProperty("useServerPrepStmts ", true);
        } else {
            String dataFolderPath = (String.format("%s/data", plugin.getDataFolder()));
            createDataFolder(dataFolderPath);
            hikariConfig.setJdbcUrl(String.format("jdbc:sqlite:%s/database.db", dataFolderPath));
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
        }
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static void disconnect() {
        hikariDataSource.close();
    }

    public static Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    private void createDataFolder(String dataFolderPath) {
        File dataFolder = new File(dataFolderPath);
        if (!dataFolder.exists())
            dataFolder.mkdir();
    }
}
