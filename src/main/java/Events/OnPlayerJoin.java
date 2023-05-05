package Events;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {
    Main plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.prefix;

        sendNoDatabaseFoundMessage(player, prefix);
        createDatabasePlayer(player);
    }

    private void sendNoDatabaseFoundMessage(Player player, String prefix) {
        if (!plugin.isConnected && player.hasPermission("kudmin")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. If you're trying to connect to a database server please check your credentials in the mysql.yml!"));
        }
    }

    private void createDatabasePlayer(Player player) {
        if (!plugin.isConnected) return;
        SQLGetter data = new SQLGetter(plugin);
        data.createPlayer(player);
    }
}
