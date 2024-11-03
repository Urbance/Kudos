package Events;

import Utils.SQL.SQLGetter;
import Utils.WorkaroundManagement;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class OnPlayerJoin implements Listener {
    private Main plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.prefix;

        sendNoDatabaseFoundMessage(player, prefix);
        if (!createDatabasePlayer(player.getUniqueId())) {
            Bukkit.getLogger().warning(prefix + "An error has occurred: No player could be created in the database. Please contact the system administrator or the developer of the plugin");
            return;
        }
        sendWorkaroundNeededMessage(player);
    }

    private void sendNoDatabaseFoundMessage(Player player, String prefix) {
        if (!plugin.isConnected && player.hasPermission("kudos.admin.*")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. If you're trying to connect to a database server please check your credentials in the mysql.yml!"));
        }
    }

    private boolean createDatabasePlayer(UUID uuid) {
        if (!plugin.isConnected) return false;
        if (Main.oldTableScheme) return true;
        SQLGetter data = new SQLGetter(plugin);
        return data.createPlayer(uuid);
    }

    private void sendWorkaroundNeededMessage(Player player) {
        if (!player.hasPermission("kudos.admin.*")) return;
        if (WorkaroundManagement.isLegacyConfig) {
            WorkaroundManagement.notifyInstanceAboutLegacyWorkaround(player);
            return;
        }
        if (WorkaroundManagement.isSQLMigrationNeeded || WorkaroundManagement.isConfigMigrationNeeded)
            WorkaroundManagement.notifyInstanceAboutWorkaround(player);
    }
}
