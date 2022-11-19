package Commands;

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
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.ArrayList;
import java.util.List;

public class Kudmin implements CommandExecutor, TabCompleter {
    public String prefix = "&7[&cKudmin&7] ";
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration locale = plugin.localeConfig;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        SQLGetter data = new SQLGetter(plugin);

        if (!sender.hasPermission("kudmin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.no-permission")));
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
                if (args.length > 1) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. For more informations see &e/kudmin help&7!"));
                    return false;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reloaded configs!"));
                plugin.reloadConfigs();
                break;

            case "add":
               if (!validateInput(args, sender))
                   return false;

               data.addKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId(), null, Integer.parseInt(args[2]));
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
                if (args.length > 2) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. For more informations see &e/kudmin help&7!"));
                    return false;
                }
                if (!ifTargetPlayerExists(sender, args)) {
                   return false;
                }
                data.clearKudos(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos from &e" + args[1]));
                break;

            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Unknown argument &e" + args[0] + "&7. Type &e/kudmin help &7to get more informations!"));
        }
    return false;
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        if (args.length > 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. For more informations see &e/kudmin help&7!"));
            return false;
        }
        if (!ifTargetPlayerExists(sender, args)) {
            return false;
        }
        if (!isValueAnInteger(sender, args)) {
            return false;
        }
        return true;
    }

    private boolean isValueAnInteger(CommandSender sender, String[] args) {
        if (args.length < 3 || !isValueAnInteger(args[2]) || !(Integer.parseInt(args[2]) >= 0)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a positive integer number!"));
            return false;
        }
        return true;
    }

    private boolean isValueAnInteger(String rawValue) {
        try {
            Integer.parseInt(rawValue);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean ifTargetPlayerExists(CommandSender sender, String[] args) {
        SQLGetter data = new SQLGetter(plugin);
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a target player!"));
            return false;
        }
        String targetPlayerName = args[1];
        OfflinePlayer targetPlayer = data.getPlayer(Bukkit.getOfflinePlayer(targetPlayerName).getUniqueId());
        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Player &e" + targetPlayerName + " &7not found!"));
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        ArrayList<String> list = new ArrayList<>();

        if (!sender.hasPermission("kudmin"))
            return list;
        if (args.length == 1) {
            list.add("help");
            list.add("add");
            list.add("remove");
            list.add("set");
            list.add("clear");
            list.add("reload");
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "add", "remove", "set", "clear":
                    for (Player players : Bukkit.getOnlinePlayers())
                        list.add(players.getName());
            }
        }
        if (args.length == 3) {
            switch (args[0]) {
                case "add", "remove", "set" -> list.add("amount");
            }
        }
        return list;
    }
}