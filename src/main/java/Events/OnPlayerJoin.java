package Events;

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
        if (!createDatabasePlayer(player.getUniqueId())) Bukkit.getLogger().warning(prefix + "An error has occurred: No player could be created in the database. Please contact the system administrator or the developer of the plugin.");
    }

    private void sendNoDatabaseFoundMessage(Player player, String prefix) {
        if (!plugin.isConnected && player.hasPermission("kudos.kudmin.*")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. If you're trying to connect to a database server please check your credentials in the mysql.yml!"));
        }
    }

    private boolean createDatabasePlayer(UUID uuid) {
        if (!plugin.isConnected) return false;
        SQLGetter data = new SQLGetter(plugin);
        return data.createPlayer(uuid);
    }
}
