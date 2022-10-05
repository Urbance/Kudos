package Commands;

import Utils.GUI;
import Utils.LocaleManager;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Kudos implements CommandExecutor {
    public static Inventory inventory;

    public SQLGetter data;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            String prefix = Main.prefix;
            FileConfiguration locale = new LocaleManager(Main.getPlugin(Main.class)).getConfig();

            if (args.length == 0) {
                if (player.hasPermission("kudos.gui") || player.hasPermission("kudos.*")) {
                    inventory = new GUI().getInventory();
                    player.openInventory(inventory);
                } else {
                    Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("other.no_permission")));
                }
            }

            if (args.length == 1) {
                data = new SQLGetter(Main.getPlugin(Main.class));

                if (player.hasPermission("kudos.show") || player.hasPermission("kudos.*")) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);

                    String showKudosMessage = locale.getString("kudos.show_player_kudos");
                    showKudosMessage = showKudosMessage.replaceAll("%targetplayer%", targetPlayer.getName());
                    showKudosMessage = showKudosMessage.replaceAll("%targetplayer_kudos%", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
                    Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + showKudosMessage));
                } else {
                    Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("other.no_permission")));
                }
            }
        } else {
            Bukkit.getServer().getLogger().info("You can't execute this command as console!");
        }
        return false;
    }
}
