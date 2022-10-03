package Utils.SQL;

import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQL {
    private Connection connection;
    private FileConfiguration config = Main.getPlugin(Main.class).getConfig();

    private String host = config.getString("SQL.hostname");
    private String port = config.getString("SQL.port");
    private String database = config.getString("SQL.database");
    private String username = config.getString("SQL.username");
    private String password = config.getString("SQL.password");
    private String useSSL = config.getString("SQL.useSSL");

    public boolean isConnected() {
        return (connection == null ? false : true);
    }

    public void connect() throws  ClassNotFoundException, SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useSSL=%s", host, port, database, username, password, useSSL));
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
