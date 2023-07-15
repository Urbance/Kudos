package Utils.SQL;

import Utils.FileManager;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class SQLGetter {
    public static String driverClassName;
    private FileConfiguration guiConfig;

    public SQLGetter(Main plugin) {
        this.guiConfig = new FileManager("gui.yml", plugin).getConfig();
    }

    public void initDatabases() {
        createPlayersTable();
        createKudosTable();
    }

    private void createPlayersTable() {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players " +
                     "(UUID VARCHAR(100) NOT NULL, TotalKudos INT NOT NULL, TotalAssignedKudos INT NOT NULL, PRIMARY KEY (UUID))")) {

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createKudosTable() {
        String createTableStatement = "CREATE TABLE IF NOT EXISTS kudos " +
                "(EntryNo INT PRIMARY KEY AUTO_INCREMENT, ToPlayer VARCHAR(100) NOT NULL, FromPlayer VARCHAR(100) NOT NULL, Reason VARCHAR(100) NOT NULL, Date VARCHAR(100) NOT NULL)";
        if (driverClassName.equals("org.sqlite.JDBC")) {
            createTableStatement = createTableStatement.replace("AUTO_INCREMENT", "AUTOINCREMENT");
            createTableStatement = createTableStatement.replace("INT", "INTEGER");
        }

        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createTableStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (UUID,TotalKudos,TotalAssignedKudos) VALUES (?,?,?)")) {
            if (!exists(uuid)) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setInt(2, 0);
                preparedStatement.setInt(3, 0);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public OfflinePlayer getPlayer(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID FROM players WHERE UUID=?")) {
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
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE UUID=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addKudos(UUID toPlayer, UUID fromPlayer, int kudos) {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO kudos (ToPlayer, FromPlayer, Reason, Date) VALUES (?,?,?,?); " + "UPDATE players SET TotalKudos=?,TotalAssignedKudos=? WHERE UUID=?;")) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();

            preparedStatement.setString(1, String.valueOf(toPlayer));
            preparedStatement.setString(2, String.valueOf(fromPlayer));
            preparedStatement.setString(3, "UNDEFINED");
            preparedStatement.setString(4, dateFormat.format(date));
            preparedStatement.executeUpdate();
            setTotalKudosAndTotalAssignedKudos(toPlayer, kudos);
            // TODO Getting obsolet?
//            if (fromPlayer != null)
//                addAssignedKudos(fromPlayer, 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setTotalKudosAndTotalAssignedKudos(UUID toPlayer, int kudos) {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET TotalKudos=?,TotalAssignedKudos=? WHERE UUID=?;")) {
            preparedStatement.setInt(1, (getKudos(toPlayer) + kudos));
            preparedStatement.setInt(2, 0);
            preparedStatement.setString(3, toPlayer.toString());
            preparedStatement.executeUpdate();

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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT TotalKudos from players WHERE UUID=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            int kudos = 0;
            if (results.next()) {
                kudos = results.getInt("TotalKudos");
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT TotalAssignedKudos from players WHERE UUID=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            int assigned = 0;
            if (results.next()) {
                assigned = results.getInt("TotalAssignedKudos");
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
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT TotalKudos, UUID FROM players ORDER BY TotalKudos DESC LIMIT 3")){
            int amountDisplayPlayers = guiConfig.getInt("slot.kudos-leaderboard.display-players");
            if (amountDisplayPlayers > 15) amountDisplayPlayers = 15;
            int counter = 0;
            ResultSet results = preparedStatement.executeQuery();
            List<String> itemLore = prepareTopPlayersKudosList(amountDisplayPlayers);

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("UUID"));
                String playerName = Bukkit.getPlayer(uuid).getName();
                String kudos = results.getString("TotalKudos");
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
        String loreFormat = guiConfig.getString("slot.kudos-leaderboard.lore-format");

        for (int entry = 0; entry < amountDisplayPlayers; entry++) {
            list.add(loreFormat);
        }
        return list;
    }

    private List<String> setNotAssignedKudosText(List<String> lore) {
        for (int entry = 0; entry < lore.size(); entry++) {
            if (lore.get(entry).contains("%top_kudos%") || lore.get(entry).contains("%top_player%")) {
                lore.set(entry, ChatColor.translateAlternateColorCodes('&', guiConfig.getString("slot.kudos-leaderboard.not-assigned-kudos")));
            }
        }
        return lore;
    }
}