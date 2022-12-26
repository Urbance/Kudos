package Utils.SQL;

import Utils.FileManager;
import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQL {
    private Connection connection;
    private FileConfiguration mysqlConfig = new FileManager("mysql.yml", Main.getPlugin(Main.class)).getConfig();

    private String host = mysqlConfig.getString("hostname");
    private String port = mysqlConfig.getString("port");
    private String database = mysqlConfig.getString("database");
    private String username = mysqlConfig.getString("username");
    private String password = mysqlConfig.getString("password");
    private String useSSL = mysqlConfig.getString("useSSL");

    public boolean isConnected() {
        return (connection == null ? false : true);
    }

    public void connect() throws  ClassNotFoundException, SQLException {
        if (!isConnected()) {
            Class.forName("com.mysql.jdbc.Driver");
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
