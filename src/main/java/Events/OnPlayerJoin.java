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

        if (player.hasPermission("kudmin")) {
            if (!databaseIsConnected(player, prefix))
                return;

            workaroundChecker(player);
        }

        createDatabasePlayer(player);
    }

    private boolean databaseIsConnected(Player player, String prefix) {
        if (!plugin.isConnected) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cNo database found. Please setup a database in the mysql.yml file!"));
            return false;
        }
        return true;
    }

    private void workaroundChecker(Player player) {
        if (plugin.workaroundChecker())
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7========= &c&lKudos Information&7=========\n" +
                    " \n" +
                    "&7It looks like you've used an older plugin version before:\n" +
                    "There are some changes on the config.yml structure.\n" +
                    "Please create a &e&lbackup &7from the config.yml\n" +
                    "and execute &e/kudmin workaround&7!"));
    }

    private void createDatabasePlayer(Player player) {
        SQLGetter data = new SQLGetter(plugin);
        data.createPlayer(player);
    }
}
