package Commands;

import Utils.GUI;
import Utils.KudosManagement;
import Utils.KudosMessage;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kudos implements CommandExecutor, TabCompleter {
    public static Inventory inventory;
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration locale;
    public SQLGetter data;
    public String prefix;
    public OfflinePlayer targetPlayer;
    public KudosManagement kudosManagement;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.prefix = plugin.prefix;
        this.locale = plugin.localeConfig;
        this.data = new SQLGetter(plugin);
        this.kudosManagement = new KudosManagement(plugin);

        if (!validateInput(args, sender))
            return false;
        if (args.length == 0) {
            openGUI(sender);
        }
        if (args.length == 1) {
            showKudos(sender);
        }
        return false;
    }

    public void openGUI(CommandSender sender) {
        if (!(sender instanceof Player)) {
            Bukkit.getServer().getLogger().info("Please use /kudos [player]");
            return;
        }
        if (!(sender.hasPermission("kudos.gui") || sender.hasPermission("kudos.*"))) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.no-permission")));
            return;
        }

        Player player = Bukkit.getPlayer(sender.getName());
        inventory = new GUI().getInventory();
        player.openInventory(inventory);
    }

    public void showKudos(CommandSender sender) {
        if (!(sender.hasPermission("kudos.show") || sender.hasPermission("kudos.*"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.no-permission")));
            return;
        }
        kudosManagement.showKudos((Player) sender, targetPlayer);
    }

    public boolean validateInput(String[] args, CommandSender sender) {
        if (args.length > 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.wrong-usage")));
            return false;
        }
        if (args.length == 1) {
            targetPlayer = data.getPlayer(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.player-not-found").replaceAll("%targetplayer%", args[0])));
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (!(sender.hasPermission("kudos.show") || sender.hasPermission("kudos.*")))
            return list;
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        return list;
    }
}
