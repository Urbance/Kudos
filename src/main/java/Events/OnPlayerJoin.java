package Events;

import Commands.Kudmin;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class OnPlayerJoin implements Listener {
    Main plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.prefix;

        sendNoDatabaseFoundMessage(player, prefix);
        sendOldTableSchemeMessage(player);
        if (!createDatabasePlayer(player.getUniqueId())) Bukkit.getLogger().warning(prefix + "An error has occurred: No player could be created in the database. Please contact the system administrator or the developer of the plugin");
    }

    private void sendNoDatabaseFoundMessage(Player player, String prefix) {
        if (!plugin.isConnected && player.hasPermission("kudos.admin.*")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. If you're trying to connect to a database server please check your credentials in the mysql.yml!"));
        }
    }

    private void sendOldTableSchemeMessage(Player player) {
        if (!Main.oldTableScheme || !player.hasPermission("kudos.admin.*")) return;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', Kudmin.prefix + "Data migration is required. Please create a &ebackup &7from the database. Perform &e/kudmin migrate &7and restart the server. The statistics of how many Kudos a player has awarded will be reset!"));
    }

    private boolean createDatabasePlayer(UUID uuid) {
        if (!plugin.isConnected) return false;
        if (Main.oldTableScheme) return true;
        SQLGetter data = new SQLGetter(plugin);
        return data.createPlayer(uuid);
    }
}
