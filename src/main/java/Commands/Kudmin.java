package Commands;

import Utils.ComponentCreator;
import Utils.FileManager;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kudmin implements CommandExecutor, TabCompleter {
    private String prefix = "&7[&cKudmin&7] ";
    private Main plugin = Main.getPlugin(Main.class);
    private FileManager localeManager = new FileManager("messages.yml", plugin);
    private FileConfiguration locale = localeManager.getConfig();
    private String optionValue;

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
            case "help" -> {
                if (!validateInput(args, sender, 1, 0, false, false, false))
                    return false;

                String helpText = "&7========= &c&lKudmin Commands &7=========\n" +
                                " \n" +
                                "&7/kudmin help\n" +
                                "&7/kudmin add &e[kudos/assigned_kudos] [player] [amount]\n" +
                                "&7/kudmin remove &e[kudos/assigned_kudos] [player] [amount]\n" +
                                "&7/kudmin set &e[kudos/assigned_kudos] [player] [amount]\n" +
                                "&7/kudmin clear &e[kudos/assigned_kudos] [player]\n" +
                                "&7/kudmin clearall &e[player]\n" +
                                "&7/kudmin reload\n" +
                                " \n" +
                                "&7You can find all commands and permissions " ;
                ComponentBuilder helpTextComponent = new ComponentCreator(helpText).createPlainTextComponent(false, null);
                ComponentBuilder wiki = new ComponentCreator("&chere&7!")
                        .createLinkTextComponent("&l&o&cClick!","https://github.com/Urbance/Kudos/wiki/How-To-Start#commands-and-permissions", false);
                sender.spigot().sendMessage(helpTextComponent
                        .append(wiki.create())
                        .create());
            }
            case "reload" -> {
                if (!validateInput(args, sender, 1, 0, false, false, false))
                    return false;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reloaded configs!"));
                plugin.reloadConfigs();
            }
            case "clear" -> {
                if (!validateInput(args, sender, 3, 2, true, true, false)) {
                    return false;
                }
                String playerName = args[2];
                UUID player = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                switch (optionValue) {
                    case "kudos" -> {
                        data.clearKudos(player);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos from &e" + playerName));
                    }
                    case "assigned_kudos" -> {
                        data.clearAssignedKudos(player);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared assigned Kudos from &e" + playerName));
                    }
                }
            }
            case "clearall" -> {
                if (!validateInput(args, sender, 2, 1, false, true, false)) {
                    return false;
                }
                String playerName = args[1];
                UUID player = Bukkit.getOfflinePlayer(playerName).getUniqueId();

                data.clearKudosAndAssignedKudos(player);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos and assigned Kudos from &e" + playerName));
            }
            case "add" -> {
                if (!validateInput(args, sender, 4, 2, true, true, true))
                    return false;

                int amount = Integer.parseInt(args[3]);
                String playerName = args[2];
                UUID player = Bukkit.getOfflinePlayer(playerName).getUniqueId();

                switch (optionValue) {
                    case "kudos" -> {
                        data.addKudos(player, null, amount);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + amount + " Kudos &7" + "to &e" + playerName));
                    }
                    case "assigned_kudos" -> {
                        data.addAssignedKudos(player, amount);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + amount + " assigned Kudos &7" + "to &e" + playerName));
                    }
                }
            }
            case "remove" -> {
                if (!validateInput(args, sender, 4, 2, true, true, true)) {
                    return false;
                }
                int amount = Integer.parseInt(args[3]);
                String playerName = args[2];
                UUID player = Bukkit.getOfflinePlayer(playerName).getUniqueId();

                switch (optionValue) {
                    case "kudos" -> {
                        if (amount > data.getKudos(player)) {
                            data.clearKudos(player);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " Kudos &7" + "from &e" + playerName));
                            return false;
                        }
                        data.removeKudos(player, amount);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " Kudos &7" + "from &e" + playerName));
                    }
                    case "assigned_kudos" -> {
                        if (amount > data.getAssignedKudo(player)) {
                            data.clearAssignedKudos(player);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " assigned Kudos &7" + "from &e" + playerName));
                            return false;
                        }
                        data.removeAssignedKudos(player, amount);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " assigned Kudos &7" + "from &e" + playerName));
                    }
                }
            }
            case "set" -> {
                if (!validateInput(args, sender, 4, 2, true, true, true)) {
                    return false;
                }
                int amount = Integer.parseInt(args[3]);
                String playerName = args[2];
                UUID player = Bukkit.getOfflinePlayer(playerName).getUniqueId();

                switch (optionValue) {
                    case "kudos" -> {
                        data.setKudos(player, amount);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Set &e" + amount + " Kudos &7" + "to &e" + playerName));
                    }
                    case "assigned_kudos" -> {
                        data.setAssignedKudos(player, amount);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + amount + " assigned Kudos &7" + "to &e" + playerName));
                    }
                }
            }
            default ->
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Unknown argument &e" + args[0] + "&7. Type &e/kudmin help &7to get more informations!"));
        }
    return false;
    }

    private boolean validateInput(String[] args, CommandSender sender, int maxArgs, int playerArgumentPosition, boolean validateOptionValue, boolean validateTargetPlayer, boolean validateValue) {
        if (args.length > maxArgs) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. For more informations see &e/kudmin help!"));
            return false;
        }
        if (validateOptionValue) {
            if (!setAndValidateOptionValue(sender, args))
                return false;
        }
        if (validateTargetPlayer) {
            if (!TargetPlayerExists(sender, args, playerArgumentPosition))
                return false;
        }
        if (validateValue) {
            if (!isValueAnInteger(sender, args))
                return false;
        }
        return true;
    }

    private boolean isValueAnInteger(CommandSender sender, String[] args) {
        if (args.length < 4 || !isValueAnInteger(args[3]) || !(Integer.parseInt(args[3]) >= 0)) {
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

    private boolean setAndValidateOptionValue(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. Please choose between &ekudos &7or &eassigned_kudos&7!"));
            return false;
        }
        optionValue = args[1];
        if (!(optionValue.equals("kudos") || optionValue.equals("assigned_kudos"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. Please choose between &ekudos &7or &eassigned_kudos&7!"));
            return false;
        }
        return true;
    }

    private boolean TargetPlayerExists(CommandSender sender, String[] args, int playerArgumentPosition) {
        SQLGetter data = new SQLGetter(plugin);
        if (args.length < playerArgumentPosition + 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a target player!"));
            return false;
        }
        String targetPlayerName = args[playerArgumentPosition];
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
            list.add("clearall");
            list.add("reload");
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "add", "remove", "set", "clear" -> {
                    list.add("kudos");
                    list.add("assigned_kudos");
                }
                case "clearall" -> {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        list.add(players.getName());
                    }
                }
            }
        }
        if (args.length == 3) {
            switch (args[0]) {
                case "add", "remove", "set", "clear":
                    for (Player players : Bukkit.getOnlinePlayers())
                        list.add(players.getName());
            }
        }
        if (args.length == 4) {
            switch (args[0]) {
                case "add", "remove", "set" -> list.add("amount");
            }
        }
        return list;
    }
}