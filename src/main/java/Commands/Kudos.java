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

import static de.urbance.Main.prefix;

public class Kudos implements CommandExecutor {
    public static Inventory inventory;
    public FileConfiguration locale;
    public SQLGetter data;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Bukkit.getServer().getLogger().info("You can't execute this command as console!");
            return false;
        }

        if (!validateInput(args, sender)) {
            return false;
        }

        if (args.length == 0) {
            openGUI(sender);
        }

        if (args.length == 1) {
            showKudos(args, sender);
        }

        return false;
    }

    public void openGUI(CommandSender sender) {
        if (!(sender.hasPermission("kudos.gui") || sender.hasPermission("kudos.*"))) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.no_permission")));
            return;
        }

        Player player = Bukkit.getPlayer(sender.getName());
        inventory = new GUI().getInventory();
        player.openInventory(inventory);
    }

    public void showKudos(String[] args, CommandSender sender) {
        data = new SQLGetter(Main.getPlugin(Main.class));

        if (!(sender.hasPermission("kudos.show") || sender.hasPermission("kudos.*"))) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.no_permission")));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        String showKudosMessage = locale.getString("kudos.show_player_kudos");
        showKudosMessage = showKudosMessage.replaceAll("%targetplayer%", targetPlayer.getName());
        showKudosMessage = showKudosMessage.replaceAll("%targetplayer_kudos%", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + showKudosMessage));
    }

    public boolean validateInput(String[] args, CommandSender sender) {
        locale = new LocaleManager(Main.getPlugin(Main.class)).getConfig();

        if (args.length > 1) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.wrong_usage")));
            return false;
        }

        if (Bukkit.getPlayer(args[0]) == null) {
            String playerNotFound = locale.getString("error.player_not_found");
            playerNotFound = playerNotFound.replaceAll("%targetplayer%", args[0]);
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + playerNotFound));
            return false;
        }

        return true;
    }
}
