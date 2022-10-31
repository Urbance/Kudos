package Events;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;

public class OnPlayerJoin implements Listener {
    SQLGetter data;
    Connection databaseConnection;
    Main plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.getConfig().getString("prefix");

        if (player.hasPermission("kudmin") && databaseConnection == null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. Please setup a database in the config.yml file!"));
            return;
        }

        data = new SQLGetter(plugin);
        data.createPlayer(player);


    }
}
