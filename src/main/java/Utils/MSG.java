package Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class MSG {
    private static String prefix = "&7[&eKudos&7] ";

    public static void send(String player, String message) {
        Bukkit.getPlayer(player).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
