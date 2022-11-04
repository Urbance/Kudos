package Commands;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.UUID;

public class Kudmin implements CommandExecutor {
    public String prefix = "&7[&cKudmin&7] ";
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration locale = plugin.locale;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SQLGetter data = new SQLGetter(plugin);

        if (!sender.hasPermission("kudmin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.no_permission")));
            return false;
        }

        if (args.length == 0) {
            PluginDescriptionFile pluginDescriptionFile = plugin.getDescription();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The plugin is running on version &c" + pluginDescriptionFile.getVersion()));
            return false;
        }

        switch (args[0]) {
            case "help":
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
        "&7========= &c&lKudmin Commands &7=========\n" +
                    " \n" +
                    "/kudmin help &7- Shows all kudmin commands\n" +
                    "/kudmin add &e[player] [amount] &7- Add Kudos\n" +
                    "/kudmin remove &e[player] [amount] &7- Remove Kudos\n" +
                    "/kudmin set &e[player] [amount] &7- Set Kudos\n" +
                    "/kudmin clear &e[player] &7- Clear all Kudos\n" +
                    "/kudmin reload - Reloads config and locales\n" +
                    " \n" +
                    "All player commands are listed on &c/kudos"));
               break;
            case "reload":
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reloaded configs!"));
                plugin.reloadConfigs();
                break;

            case "add":
               if (!validateInput(args, sender))
                   return false;

               UUID uuid;
               if (!(sender instanceof Player)) {
                   uuid = null;
               } else {
                   uuid = ((Player) sender).getUniqueId();
               }

               data.addKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId(), uuid, Integer.parseInt(args[2]));
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + args[2] + " Kudos &7" + "to &e" + args[1]));
               break;

            case "remove":
               if (!validateInput(args, sender)) {
                   return false;
               }

               if (Integer.parseInt(args[2]) > data.getKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId())) {
                   data.clearKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                   sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + args[2] + " Kudos &7" + "to &e" + args[1]));
                   return false;
               }

               data.removeKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId(), Integer.parseInt(args[2]));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + args[2] + " Kudos &7" + "to &e" + args[1]));
               break;

            case "set":
               if (!validateInput(args, sender)) {
                   return false;
               }

               data.setKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId(), Integer.parseInt(args[2]));
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Set &e" + args[2] + " Kudos &7" + "to &e" + args[1]));
               break;

            case "clear":
               if (args.length != 2) {
                   sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Wrong usage! Please try &e/kudmin help"));
                   return false;
               }

                OfflinePlayer player = data.getPlayer(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                if (player == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Wrong usage! Player &e" + args[1] + " &7not found"));
                    return false;
                }
               data.clearKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos from &e" + args[1]));
               break;
       }
    return false;
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        SQLGetter data = new SQLGetter(plugin);

        if (args.length < 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Wrong usage! Please try &e/kudmin help"));
            return false;
        }

        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a positive integer number"));
            return false;
        }

        if (Integer.parseInt(args[2]) < 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a number that is greater than 0"));
            return false;
        }

        OfflinePlayer player = data.getPlayer(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
        if (player == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Wrong usage! Player &e" + args[1] + " &7not found"));
            return false;
        }
        return true;
    }
}