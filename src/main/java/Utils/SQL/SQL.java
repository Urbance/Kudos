package Utils.SQL;

import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
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
            if (config.getBoolean("general.use-SQL")) {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useSSL=%s", host, port, database, username, password, useSSL));
            } else {
                Class.forName("org.sqlite.JDBC");
                String dataFolderPath = (String.format("%s/data", plugin.getDataFolder()));
                File dataFolder = new File(dataFolderPath);
                if (!dataFolder.exists())
                    dataFolder.mkdir();
                connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s/database.db", dataFolderPath));
            }
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
}
