package Utils.SQL;

import Utils.FileManager;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SQLGetter {
    private Main plugin;
    private FileConfiguration guiConfig;

    public SQLGetter(Main plugin) {
        this.plugin = plugin;
        this.guiConfig = new FileManager("gui.yml", plugin).getConfig();
    }

    public void createTable() {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS kudos " +
                "(UUID VARCHAR(100),Name VARCHAR(100),Kudos INT(100),Assigned INT(100),PRIMARY KEY (UUID))")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(Player player) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO kudos (Name,UUID,Kudos,Assigned) VALUES (?,?,?,?)")) {
            UUID uuid = player.getUniqueId();
            if (!exists(uuid)) {
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Name=? WHERE UUID=?")) {
            preparedStatement.setString(1, player.getName());
            preparedStatement.setString(2, String.valueOf(uuid));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public OfflinePlayer getPlayer(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID FROM kudos WHERE UUID=?")) {
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM kudos WHERE UUID=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addKudos(UUID toPlayer, UUID fromPlayer, int kudos) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?");) {
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?")) {
            preparedStatement.setInt(1, (getKudos(uuid) - kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setKudos(UUID uuid, int kudos) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?")) {
            preparedStatement.setInt(1, (kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Kudos=? WHERE UUID=?")) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT Kudos from kudos WHERE UUID=?")) {
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?")) {
            preparedStatement.setInt(1, (getAssignedKudo(uuid) + assigned));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeAssignedKudos(UUID uuid, int kudos) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?")) {
            preparedStatement.setInt(1, (getKudos(uuid) - kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setAssignedKudos(UUID uuid, int kudos) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?")) {
            preparedStatement.setInt(1, (kudos));
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getAssignedKudo(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT Assigned from kudos WHERE UUID=?")) {
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Assigned=? WHERE UUID=?")) {
            preparedStatement.setInt(1, 0);
            preparedStatement.setString(2, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearKudosAndAssignedKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE kudos SET Kudos=0, Assigned=0 WHERE UUID=?")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getTopPlayersKudos() {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT Kudos, Name FROM kudos ORDER BY Kudos DESC LIMIT 3")){
            int amountDisplayPlayers = guiConfig.getInt("slot.top-kudos-players.display-players");
            if (amountDisplayPlayers > 15) amountDisplayPlayers = 15;
            int counter = 0;
            ResultSet results = preparedStatement.executeQuery();
            List<String> itemLore = prepareTopPlayersKudosList(amountDisplayPlayers);

            while (results.next()) {
                String playerName = results.getString("NAME");
                String kudos = results.getString("KUDOS");
                String loreEntry = itemLore.get(counter);

                loreEntry = loreEntry.replaceAll("%top_kudos%", kudos);
                loreEntry = loreEntry.replaceAll("%top_player%", playerName);

                itemLore.set(counter, loreEntry);
                counter ++;
            }

            return setNotAssignedKudosText(itemLore);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<String> prepareTopPlayersKudosList(int amountDisplayPlayers) {
        List<String> list = new ArrayList<>();
        String loreFormat = guiConfig.getString("slot.top-kudos-players.lore-format");

        for (int entry = 0; entry < amountDisplayPlayers; entry++) {
            list.add(loreFormat);
        }
        return list;
    }

    private List<String> setNotAssignedKudosText(List<String> lore) {
        for (int entry = 0; entry < lore.size(); entry++) {
            if (lore.get(entry).contains("%top_kudos%") || lore.get(entry).contains("%top_player%")) {
                lore.set(entry, ChatColor.translateAlternateColorCodes('&', guiConfig.getString("slot.top-kudos-players.not-assigned-kudos")));
            }
        }
        return lore;
    }
}