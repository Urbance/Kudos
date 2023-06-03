package Utils.SQL;

import com.zaxxer.hikari.HikariDataSource;
import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class SQL {
    private Connection connection;
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

    public boolean isConnected() {
        return (connection == null ? false : true);
    }

    public void connect() throws  ClassNotFoundException, SQLException {
        if (!isConnected()) {
            HikariDataSource dataSource = new HikariDataSource();
            dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
            dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            dataSource.addDataSourceProperty("cachePrepStmts", "true");
            dataSource.addDataSourceProperty("useServerPrepStmts", "true");
            if (config.getBoolean("general.use-SQL")) {
                dataSource.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s?&useSSL=%s", host, port, database, useSSL));
                dataSource.setUsername(username);
                dataSource.setPassword(password);
                dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            } else {
                String dataFolderPath = (String.format("%s/data", plugin.getDataFolder()));
                createDataFolder(dataFolderPath);
                dataSource.setJdbcUrl(String.format("jdbc:sqlite:%s/database.db", dataFolderPath));
                dataSource.setDriverClassName("org.sqlite.JDBC");
            }
            connection = dataSource.getConnection();
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createDataFolder(String dataFolderPath) {
        File dataFolder = new File(dataFolderPath);
        if (!dataFolder.exists())
            dataFolder.mkdir();
    }
}
