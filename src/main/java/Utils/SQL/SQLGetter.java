package Utils.SQL;

import de.urbance.Main;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLGetter {

    private Main plugin;

    public SQLGetter(Main plugin) {
        this.plugin = plugin;
    }

    public void createTable() {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS kudos " +
                    "(UUID VARCHAR(100),NAME VARCHAR(100),KUDOS INT(100),PRIMARY KEY (UUID) )");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(Player player) {
        try {
            UUID uuid = player.getUniqueId();
            if (!exists(uuid)) {
                PreparedStatement preparedStatement2 = plugin.SQL.getConnection().prepareStatement("INSERT IGNORE INTO kudos (NAME,UUID) VALUES (?,?)");
                preparedStatement2.setString(1, player.getName());
                preparedStatement2.setString(2, uuid.toString());
                preparedStatement2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT * FROM kudos WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addKudos(UUID uuid, int kudos) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET KUDOS=? WHERE UUID=?");
            preparedStatement.setInt(1, (getKudos(uuid) + kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getKudos(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT KUDOS from kudos WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            int kudos = 0;
            if (results.next()) {
                kudos = results.getInt("KUDOS");
                return kudos;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
