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

    public boolean initDatabases() {
        return createPlayersTable() && createKudosTable();
    }

    private boolean createPlayersTable() {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players " +
                     "(UUID VARCHAR(100) NOT NULL, PRIMARY KEY (UUID))")) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean createKudosTable() {
        String createTableStatement = "CREATE TABLE IF NOT EXISTS kudos " +
                "(EntryNo INT PRIMARY KEY AUTO_INCREMENT, AwardedToPlayer VARCHAR(100) NOT NULL, ReceivedFromPlayer VARCHAR(100) NOT NULL, Reason VARCHAR(100), Date VARCHAR(100) NOT NULL)";
        if (driverClassName.equals("org.sqlite.JDBC")) {
            createTableStatement = createTableStatement.replace("AUTO_INCREMENT", "AUTOINCREMENT");
            createTableStatement = createTableStatement.replace("INT", "INTEGER");
        }
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(createTableStatement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createPlayer(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (UUID) VALUES (?)")) {
            if (!exists(uuid)) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    public boolean addKudos(UUID awardedToPlayer, String receivedFromPlayer, String reason) {
        if (receivedFromPlayer == null) receivedFromPlayer = "UNDEFINED";
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO kudos (AwardedToPlayer, ReceivedFromPlayer, Reason, Date) VALUES (?,?,?,?);")) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            preparedStatement.setString(1, String.valueOf(awardedToPlayer));
            preparedStatement.setString(2, receivedFromPlayer);
            preparedStatement.setString(3, reason);
            preparedStatement.setString(4, dateFormat.format(date));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    public boolean clearKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM kudos WHERE AwardedToPlayer=?")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean clearAssignedKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM kudos WHERE ReceivedFromPlayer=?")) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean clearKudosAndAssignedKudos(UUID uuid) {
        return clearAssignedKudos(uuid) && clearKudos(uuid);
    }

    public int getKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(AwardedToPlayer) FROM kudos WHERE AwardedToPlayer=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            results.next();
            return results.getInt("COUNT(AwardedToPlayer)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAssignedKudos(UUID uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(ReceivedFromPlayer) FROM kudos WHERE ReceivedFromPlayer=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            results.next();
            return results.getInt("COUNT(ReceivedFromPlayer)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getTopPlayersKudos() {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT AwardedToPlayer, COUNT(EntryNo) FROM kudos GROUP BY AwardedToPlayer ORDER BY COUNT(EntryNo) DESC LIMIT 3")){
            int amountDisplayPlayers = guiConfig.getInt("slot.kudos-leaderboard.display-players");
            if (amountDisplayPlayers > 15) amountDisplayPlayers = 15;
            int counter = 0;
            ResultSet results = preparedStatement.executeQuery();
            List<String> itemLore = prepareTopPlayersKudosList(amountDisplayPlayers);

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("AwardedToPlayer"));
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                String kudos = results.getString("COUNT(EntryNo)");
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