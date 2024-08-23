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
    public static String consoleCommandSenderPrefix = "%ConsoleCommandSender%";
    private Main plugin;
    private FileConfiguration config;

    public SQLGetter(Main plugin) {
        this.plugin = Main.getPlugin(Main.class);
        this.config = plugin.config;
    }

    public boolean initTables() {
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
                "(KudoID INT PRIMARY KEY AUTO_INCREMENT, AwardedToPlayer VARCHAR(100) NOT NULL, ReceivedFromPlayer VARCHAR(100) NOT NULL, Reason VARCHAR(100), Date VARCHAR(100) NOT NULL)";
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

    public boolean addKudos(UUID awardedToPlayer, String receivedFromPlayer, String reason, int amount) {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO kudos (AwardedToPlayer, ReceivedFromPlayer, Reason, Date) VALUES (?,?,?,?);")) {

            for (int counter = 1; counter <= amount; counter++) {
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                preparedStatement.setString(1, String.valueOf(awardedToPlayer));
                preparedStatement.setString(2, receivedFromPlayer);
                preparedStatement.setString(3, reason);
                preparedStatement.setString(4, dateFormat.format(date));
                preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean removeKudo(int kudosID) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM kudos WHERE KudoID=?")) {
            preparedStatement.setInt(1, kudosID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

    public List<String> getAllPlayerKudos(UUID uuid) {
        List<String> kudos = new ArrayList<>();
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `kudos` WHERE AwardedToPlayer=?;")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();

            while (results.next()) {
                String entryNumber = results.getString("KudoID");
                String receivedFromPlayer;

                try {
                    receivedFromPlayer = Bukkit.getOfflinePlayer(UUID.fromString(results.getString("ReceivedFromPlayer"))).getName();
                } catch (Exception e) {
                    receivedFromPlayer = results.getString("ReceivedFromPlayer");
                }

                String reason = results.getString("Reason");
                String date = results.getString("Date");
                if (reason == null) reason = config.getString("kudo-award.general-settings.no-reason-given");
                if (receivedFromPlayer.equals(SQLGetter.consoleCommandSenderPrefix)) receivedFromPlayer = receivedFromPlayer.replace(SQLGetter.consoleCommandSenderPrefix, config.getString("general-settings.console-name"));

                kudos.add(String.format("&eID&7: %s | &efrom &7%s | &eat&7 %s \n&eReason: &7%s", entryNumber, receivedFromPlayer, date, reason));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kudos;
    }

    public HashMap<Integer, String> getPlayerReceivedKudosGUI(UUID uuid) {
        HashMap<Integer, String> receivedKudos = new HashMap<>();
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `kudos` WHERE AwardedToPlayer=? ORDER BY KudoID DESC;")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            int entryNumber = 0;
            while (results.next()) {
                String receivedFromPlayer;

                try {
                    receivedFromPlayer = Bukkit.getOfflinePlayer(UUID.fromString(results.getString("ReceivedFromPlayer"))).getName();
                } catch (Exception e) {
                    receivedFromPlayer = results.getString("ReceivedFromPlayer");
                }

                String reason = results.getString("Reason");
                String date = results.getString("Date");

                if (reason == null) reason = config.getString("kudo-award.general-settings.no-reason-given");

                receivedKudos.put(entryNumber, receivedFromPlayer + "@" + reason + "@" + date);
                entryNumber++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receivedKudos;
    }

    public int getPlayerKudo(int requestedKudosID) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT KudoID FROM kudos WHERE KudoID=?;")) {
            preparedStatement.setInt(1, requestedKudosID);
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getInt("KudoID");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getAmountKudos(UUID uuid) {
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

    // TODO: remove function specific coded entries amount: make entries dynamically
    public List<String> getTopPlayersKudosTemp() {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT AwardedToPlayer, COUNT(KudoID) FROM kudos GROUP BY AwardedToPlayer ORDER BY COUNT(KudoID) DESC LIMIT 3")){
            int amountDisplayPlayers = plugin.globalGuiSettingsConfig.getInt("placeholderapi-settings.items.kudos-leaderboard.display-players");
            if (amountDisplayPlayers > 15) amountDisplayPlayers = 15;
            int counter = 0;
            ResultSet results = preparedStatement.executeQuery();
            List<String> itemLore = prepareTopPlayersKudosList(amountDisplayPlayers);

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("AwardedToPlayer"));
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                String kudos = results.getString("COUNT(KudoID)");
                String loreEntry = itemLore.get(counter);

                loreEntry = loreEntry.replaceAll("%top_kudos%", kudos);
                loreEntry = loreEntry.replaceAll("%top_player%", playerName);

                itemLore.set(counter, loreEntry);
                counter++;
            }

            return setNotAssignedKudosText(itemLore);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public HashMap<UUID, String> getTopPlayersKudos(int amountPlayers) {
        HashMap<UUID, String> playerKudos = new HashMap<>();

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT AwardedToPlayer, COUNT(KudoID) FROM kudos GROUP BY AwardedToPlayer ORDER BY COUNT(KudoID) DESC LIMIT " + amountPlayers)) {
            ResultSet results = preparedStatement.executeQuery();

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("AwardedToPlayer"));
                String kudos = results.getString("COUNT(KudoID)");

                playerKudos.put(uuid, kudos);
            }
            return playerKudos;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerKudos;
    }

    private List<String> prepareTopPlayersKudosList(int amountDisplayPlayers) {
        List<String> list = new ArrayList<>();
        String loreFormat = plugin.globalGuiSettingsConfig.getString("placeholderapi-settings.items.kudos-leaderboard.item-lore-format");

        for (int entry = 0; entry < amountDisplayPlayers; entry++) {
            list.add(loreFormat);
        }
        return list;
    }

    private List<String> setNotAssignedKudosText(List<String> lore) {
        for (int entry = 0; entry < lore.size(); entry++) {
            if (lore.get(entry).contains("%top_kudos%") || lore.get(entry).contains("%top_player%")) {
                lore.set(entry, ChatColor.translateAlternateColorCodes('&', plugin.globalGuiSettingsConfig.getString("placeholderapi-settings.items.kudos-leaderboard.item-lore-not-assigned-kudos")));
            }
        }
        return lore;
    }
}