package Events;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {
    SQLGetter data;
    Main plugin = Main.getPlugin(Main.class);

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.getConfig().getString("prefix");

        if (player.hasPermission("kudmin")) {
            if (!plugin.isConnected) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. Please setup a database in the mysql.yml file!"));
                return;
            }
            if (plugin.workaroundChecker())
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7========= &c&lKudos Information&7=========\n" +
                        " \n" +
                        "&7It looks like you've used an older plugin version before:\n" +
                        "There are some changes on the config.yml structure.\n" +
                        "Please create a &e&lbackup &7from the config.yml\n" +
                        "and execute &e/kudmin workaround&7!"));
        }
        data = new SQLGetter(plugin);
        data.createPlayer(player);
    }
}
