package Utils.SQL;

import Utils.FileManager;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class SQLGetter {

    private Main plugin;
    public FileConfiguration guiConfig;

    public SQLGetter(Main plugin) {
        this.plugin = plugin;
        this.guiConfig = new FileManager("gui.yml", plugin).getConfig();
    }

    public void createTable() {
        PreparedStatement preparedStatement;
        try {
            preparedStatement = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS kudos " +
                    "(UUID VARCHAR(100),Name VARCHAR(100),Kudos INT(100),Assigned INT(100),PRIMARY KEY (UUID))");
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(Player player) {
        try {
            UUID uuid = player.getUniqueId();
            if (!exists(uuid)) {
                PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("INSERT INTO kudos (Name,UUID,Kudos,Assigned) VALUES (?,?,?,?)");
                preparedStatement.setString(1, player.getName());
                preparedStatement.setString(2, uuid.toString());
                preparedStatement.setInt(3, 0);
                preparedStatement.setInt(4, 0);
                preparedStatement.executeUpdate();
            }
            // TODO Query which checks username with username on database?
            updateUsername(player, uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateUsername(Player player, UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Name=? WHERE UUID=?");
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, String.valueOf(uuid));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public OfflinePlayer getPlayer(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT UUID FROM kudos WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return Bukkit.getOfflinePlayer(UUID.fromString(results.getString("UUID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
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

    public void addKudos(UUID toPlayer, UUID fromPlayer, int kudos) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?");
            preparedStatement.setInt(1, (getKudos(toPlayer) + kudos));
            preparedStatement.setString(2, toPlayer.toString());
            preparedStatement.executeUpdate();
            if (fromPlayer != null)
                addAssignedKudos(fromPlayer, 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeKudos(UUID uuid, int kudos) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?");
            preparedStatement.setInt(1, (getKudos(uuid) - kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setKudos(UUID uuid, int kudos) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?");
            preparedStatement.setInt(1, (kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearKudos(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?");
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getKudos(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT Kudos from kudos WHERE UUID=?");
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

    public void addAssignedKudos(UUID uuid, int assigned) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?");
            preparedStatement.setInt(1, (getAssignedKudo(uuid) + assigned));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeAssignedKudos(UUID uuid, int kudos) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?");
            preparedStatement.setInt(1, (getKudos(uuid) - kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAssignedKudos(UUID uuid, int kudos) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?");
            preparedStatement.setInt(1, (kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getAssignedKudo(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT Assigned from kudos WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            int assigned = 0;
            if (results.next()) {
                assigned = results.getInt("ASSIGNED");
                return assigned;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void clearAssignedKudos(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?");
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearKudosAndAssignedKudos(UUID uuid) {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("UPDATE kudos SET Kudos=0, Assigned=0 WHERE UUID=?");
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTopThreePlayers() {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT Kudos, Name FROM kudos ORDER BY Kudos DESC LIMIT 3");
            ResultSet results = preparedStatement.executeQuery();
            List<String> topThree = guiConfig.getStringList("slot.top3.lore");

            int counter = 0;
            while (results.next()) {
                topThree.set(counter, topThree.get(counter).replaceAll("%top_kudos%", results.getString("KUDOS")));
                topThree.set(counter, topThree.get(counter).replaceAll("%top_player%", results.getString("NAME")));
                counter++;
            }

            for (int i = 0; i < topThree.size(); i++) {
                if (topThree.get(i).contains("%top_kudos%") || topThree.get(i).contains("%top_player%")) {
                    topThree.set(i, ChatColor.translateAlternateColorCodes('&', guiConfig.getString("slot.top3.not-assigned")));
                }
            }

            return topThree;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void keepAlive() {
        try {
            PreparedStatement preparedStatement = plugin.SQL.getConnection().prepareStatement("SELECT 1 FROM kudos");
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}