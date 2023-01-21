package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

/*
 * New util for all messages that belongs to Kudos.
 * It sets the plugin prefix, placeholders (later) and formatting codes are considered.
 */
public class KudosMessage {
    Main plugin;
    String prefix;
    SQLGetter data;
    FileConfiguration locale;

    public KudosMessage(Main plugin) {
        this.plugin = Main.getPlugin(Main.class);
        this.prefix = plugin.prefix;
        this.data = new SQLGetter(plugin);
        this.locale = plugin.localeConfig;
    }

    public void send(Player player, String message) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void sendSender(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void broadcast(String message) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }

    public void noPermission(Player player) {
        send(player, locale.getString("error.no-permission"));
    }

    public void wrongUsage(Player player) {
        send(player, locale.getString("error.wrong-usage"));
    }

    public String setPlaceholders(String message, Map<String, String> values) {
        return StringSubstitutor.replace(message, values, "%", "%");
    }

    private void playSound() {
    }
}
