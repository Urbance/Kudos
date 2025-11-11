package Utils.SQL;

import Utils.ConfigManagement;
import Utils.UrbanceDebug;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

public class SQLGetter {
    public static String driverClassName;
    public static String consoleCommandSenderPrefix = "%ConsoleCommandSender%";
    private Main plugin;
    private FileConfiguration config;

    public SQLGetter(Main plugin) {
        this.plugin = Main.getPlugin(Main.class);
        this.config = ConfigManagement.getConfig();
    }

    public boolean initTables() {
        return createPlayersTable() && createKudosTable();
    }

    public boolean oldDateFormatUsed() {
        String rawFirstAwardedKudoDate = null;

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT Date FROM `kudos` ORDER BY Date ASC LIMIT 1;")) {
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                rawFirstAwardedKudoDate = results.getString("Date");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        if (rawFirstAwardedKudoDate == null) return false;

        try {
            LocalDateTime.parse(rawFirstAwardedKudoDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeException e) {
            return true;
        }
        return false;
    }

    public boolean convertOldDateFormatToNewDateFormat() {
        UrbanceDebug.sendInfo("Step: SQLGetter.ConvertOldDateFormatToNewDateFormat");

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT KudoID, Date FROM kudos")) {
            ResultSet results = preparedStatement.executeQuery();
            PreparedStatement updateDatesPreparedStatement = connection.prepareStatement("UPDATE kudos SET Date=? WHERE KudoID=?");

            int updatedRows = 0;

            while (results.next()) {
                String kudoID = results.getString("KudoID");
                String oldDate = results.getString("Date");

                DateTimeFormatter oldDateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                LocalDateTime oldDateTime = LocalDateTime.parse(oldDate, oldDateTimeFormat);

                String newDate = oldDateTime.format(DateTimeFormatter.ISO_DATE_TIME) + ".000000000";

                updateDatesPreparedStatement.setString(1, newDate);
                updateDatesPreparedStatement.setString(2, kudoID);

                updatedRows += updateDatesPreparedStatement.executeUpdate();
            }

            UrbanceDebug.sendInfo("updatedRows: " + updatedRows);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean createPlayersTable() {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS players " +
                     "(UUID VARCHAR(100) NOT NULL, DisplayName VARCHAR(100) NOT NULL, PRIMARY KEY (UUID))")) {
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

    public boolean updatePlayer(UUID uuid) {
        if (!columnExists("players", "DisplayName"))
            addColumn("players", "DisplayName", "VARCHAR(100)", "undefined", true);

        if (exists(uuid))
            return updateDisplayName(uuid);

        return createPlayer(uuid);
    }

    private boolean createPlayer(UUID uuid) {
        String displayName = "";
        if (Bukkit.getPlayer(uuid) != null) {
            displayName = Bukkit.getPlayer(uuid).getName();
        } else {
            displayName = Bukkit.getOfflinePlayer(uuid).getName();
        }

        if (!columnExists("players", "DisplayName"))
            addColumn("players", "DisplayName", "VARCHAR(100)", "undefined", true);

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (UUID, DisplayName) VALUES (?,?)")) {
            if (!exists(uuid)) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setString(2, displayName);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return exists(uuid) && getPlayerDisplayName(String.valueOf(uuid)).equals(displayName);
    }

    private boolean updateDisplayName(UUID uuid) {
        if (!columnExists("players","DisplayName"))
            addColumn("players", "DisplayName", "VARCHAR(100)", "undefined", true);

        String displayName = uuid.toString();

        try {
            if (Bukkit.getOfflinePlayer(uuid).hasPlayedBefore())
                displayName = Bukkit.getOfflinePlayer(uuid).getName();
        } catch (NoSuchElementException ignored) {
        }

        if (!exists(uuid))
            return createPlayer(uuid);

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET DisplayName=? WHERE UUID=?")) {
            preparedStatement.setString(1, displayName);
            preparedStatement.setString(2, uuid.toString());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String databasePlayerDisplayName = getPlayerDisplayName(uuid.toString());

        if (databasePlayerDisplayName.equals(displayName))
            return true;

        Bukkit.getLogger().warning("Something went wrong during updating the display name. Please contact the plugin developer.");
        Bukkit.getLogger().warning("displayName: " + displayName);
        Bukkit.getLogger().warning("databasePlayerDisplayName: " + databasePlayerDisplayName);

        return false;
    }

    // TODO: Check what happens if there are more than 1 results
    public UUID getPlayerByDisplayName(String displayName) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID FROM players WHERE DisplayName=?")) {
            preparedStatement.setString(1, displayName);
            ResultSet results = preparedStatement.executeQuery();

            if (results.next())
                return UUID.fromString(results.getString("UUID"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPlayerDisplayName(String uuid) {
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT DisplayName FROM players WHERE UUID=?")) {
            preparedStatement.setString(1, uuid);
            ResultSet results = preparedStatement.executeQuery();

            if (results.next())
                return results.getString("DisplayName");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "undefined";
    }

    private boolean addColumn(String table, String columnName, String datatype, String defaultValue, boolean setNotNull) {
        String statement = "";
        String parameterSetNotNull = "NOT NULL";
        String parameterDefaultValue = "DEFAULT " +  "\"" + defaultValue + "\"";

        if (!setNotNull) parameterSetNotNull = "";
        if (defaultValue.isBlank()) parameterDefaultValue = "";

        statement = String.format("ALTER TABLE %s ADD COLUMN %s %s %s %s", table, columnName, datatype, parameterSetNotNull, parameterDefaultValue);

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        // Update DisplayName for top 10 total kudos players
        ArrayList<String> playerUUIDS = new ArrayList<>();

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT AwardedToPlayer FROM kudos GROUP BY AwardedToPlayer ORDER BY COUNT(KudoID) DESC LIMIT 10")) {
            ResultSet results = preparedStatement.executeQuery();

            while (results.next())
                playerUUIDS.add(results.getString("AwardedToPlayer"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (String uuid : playerUUIDS) {
            updateDisplayName(UUID.fromString(uuid)); // check if this is right
        }

        return columnExists("players", columnName);
    }

    private boolean columnExists(String tableName, String columnName) {
        String statement = "";

        if (driverClassName.equals("org.sqlite.JDBC")) {
            statement = String.format("PRAGMA table_info(%s)", tableName);

            try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                ResultSet results = preparedStatement.executeQuery();

                while (results.next()) {
                    String resultColumnName = results.getString("name");

                    if (Objects.equals(resultColumnName, columnName))
                        return true;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {
            statement = String.format("SELECT COUNT(*) FROM information_schema.columns WHERE table_name = '%s' AND column_name = '%s';", tableName, columnName);

            try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                ResultSet results = preparedStatement.executeQuery();

                if (results.next())
                    return results.getInt("COUNT(*)") == 1;

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public String getLastKudoAwardedDateFromPlayer(UUID uuid) {
        if (!exists(uuid)) {
            UrbanceDebug.sendInfo("Player with UUID " + uuid + " not found in players table");
            return null;
        }

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT Date FROM `kudos` WHERE ReceivedFromPlayer=? ORDER BY Date DESC LIMIT 1;")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();
            if (results.next()) {
                return results.getString("Date");
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

    public boolean existsByDisplayName(String displayName) {
        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE DisplayName=?")) {
            preparedStatement.setString(1, displayName);
            ResultSet results = preparedStatement.executeQuery();

            if (results.next())
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Bukkit.getOfflinePlayer(displayName).hasPlayedBefore()) {
            UUID uuid = Bukkit.getOfflinePlayer(displayName).getUniqueId();
            updatePlayer(uuid);

            return existsByDisplayName(displayName);
        }

        return false;
    }

    public boolean addKudos(UUID awardedToPlayer, String receivedFromPlayer, String reason, int amount) {
        int affectedRows = 0;

        try (Connection connection = SQL.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO kudos (AwardedToPlayer, ReceivedFromPlayer, Reason, Date) VALUES (?,?,?,?);")) {

            for (int counter = 1; counter <= amount; counter++) {
                preparedStatement.setString(1, String.valueOf(awardedToPlayer));
                preparedStatement.setString(2, receivedFromPlayer);
                preparedStatement.setString(3, reason);
                preparedStatement.setString(4, LocalDateTime.now().toString());

                affectedRows = preparedStatement.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return affectedRows > 0;
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

                LocalDateTime formattedDate = LocalDateTime.parse(date);
                date = formattedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

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

    public int getTotalAwardedKudos(UUID uuid) {
        if (!exists(uuid)) {
            UrbanceDebug.sendInfo("Player with UUID " + uuid + " not found in players table");
            return -1;
        }

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(ReceivedFromPlayer) FROM kudos WHERE ReceivedFromPlayer=?")) {
            preparedStatement.setString(1, uuid.toString());
            ResultSet results = preparedStatement.executeQuery();

            if (results.next())
                return results.getInt("COUNT(ReceivedFromPlayer)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
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
            int amountDisplayPlayers = ConfigManagement.getGlobalGuiSettingsConfig().getInt("placeholderapi-settings.items.kudos-leaderboard.display-players");
            if (amountDisplayPlayers > 15) amountDisplayPlayers = 15;
            int counter = 0;
            ResultSet results = preparedStatement.executeQuery();
            List<String> itemLore = prepareTopPlayersKudosList(amountDisplayPlayers);

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("AwardedToPlayer"));

                String playerName = getPlayerDisplayName(String.valueOf(uuid));
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
        String loreFormat = ConfigManagement.getGlobalGuiSettingsConfig().getString("placeholderapi-settings.items.kudos-leaderboard.item-lore-format");

        for (int entry = 0; entry < amountDisplayPlayers; entry++) {
            list.add(loreFormat);
        }
        return list;
    }

    private List<String> setNotAssignedKudosText(List<String> lore) {
        for (int entry = 0; entry < lore.size(); entry++) {
            if (lore.get(entry).contains("%top_kudos%") || lore.get(entry).contains("%top_player%")) {
                lore.set(entry, ChatColor.translateAlternateColorCodes('&', ConfigManagement.getGlobalGuiSettingsConfig().getString("placeholderapi-settings.items.kudos-leaderboard.item-lore-not-assigned-kudos")));
            }
        }
        return lore;
    }

    public boolean checkIfKudosTableHasOldTableSchematic() {
        String statement = "SELECT COUNT(*) AS ENTRIES FROM pragma_table_info('kudos') WHERE name='Kudos' or name='Assigned'";
        boolean useMySQL = config.getBoolean("general-settings.use-MySQL");

        if (config.isConfigurationSection("general")) {
            useMySQL = config.getBoolean("general.use-SQL");
        }

        if (useMySQL) {
            new ConfigManagement("mysql.yml", plugin);
            FileConfiguration mysqlConfig = ConfigManagement.getMySQLConfig();
            String databaseName = mysqlConfig.getString("database");
            statement = ("SELECT COUNT(*) AS ENTRIES FROM information_schema.columns\n" +
                    "WHERE TABLE_SCHEMA='%s'\n" +
                    "AND TABLE_NAME='kudos'\n" +
                    "AND COLUMN_NAME='UUID'\n" +
                    "OR TABLE_SCHEMA='%s'\n" +
                    "AND TABLE_NAME='kudos'\n" +
                    "AND COLUMN_NAME='Kudos';").replace("%s", databaseName);

            try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                ResultSet results = preparedStatement.executeQuery();
                while (results.next()) {
                    int result = results.getInt("ENTRIES");
                    if (result > 0) return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                int result = results.getInt("ENTRIES");
                if (result > 0) return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean migrateOldTableSchemeToNewTableScheme() {
        HashMap<UUID, Integer> oldKudosTablePlayerData = new HashMap<>();

        // get old kudos table player data
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT UUID, Kudos FROM kudos;")) {
            ResultSet results = preparedStatement.executeQuery();
            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("UUID"));
                int totalKudos = results.getInt("Kudos");
                oldKudosTablePlayerData.put(uuid, totalKudos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // remove old kudos table
        try (Connection connection = SQL.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE kudos")) {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // init tables
        if (!initTables()) return false;

        // migrate old player data
        for (Map.Entry<UUID, Integer> entry : oldKudosTablePlayerData.entrySet()) {
            UUID uuid = entry.getKey();
            int totalKudos = entry.getValue();
            createPlayer(uuid);
            addKudos(uuid, SQLGetter.consoleCommandSenderPrefix, null, totalKudos);
        }
        return true;
    }

}