package Utils.KudosUtils;

import Utils.ConfigManagement;
import de.urbance.Main;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Framework which handles messages from Kudos
 */
public class KudosMessage {
    private String prefix;
    private FileConfiguration locale;

    public KudosMessage(Main plugin) {
        this.prefix = plugin.prefix;
        this.locale = ConfigManagement.getLocalesConfig();
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

    public void noPermission(CommandSender sender) {
        sendSender(sender, locale.getString("error.no-permission"));
    }

    public void wrongUsage(CommandSender sender) {
        sendSender(sender, locale.getString("error.wrong-usage"));
    }

    public String setPlaceholders(String message, Map<String, String> values) {
        return StringSubstitutor.replace(message, values, "%", "%");
    }

    public static String formatStringForConsole(String message) {
        return "\n " + message.replaceAll("&.", "");
    }
}
