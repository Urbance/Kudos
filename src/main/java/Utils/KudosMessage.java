package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/*
 * New util for all messages that belongs to Kudos.
 * It sets the plugin prefix, placeholders (later) and formatting codes are considered.
 */
public class KudosMessage {
    public Main plugin;
    public String prefix;
    public SQLGetter data;

    public KudosMessage(Main plugin) {
        this.plugin = Main.getPlugin(Main.class);
        this.prefix = plugin.prefix;
        this.data = new SQLGetter(plugin);
    }

    public void send(Player targetPlayer, String message) {
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    private String setPlaceholders(String message) {
        return message;
    }

    private void playSound() {
    }
}
