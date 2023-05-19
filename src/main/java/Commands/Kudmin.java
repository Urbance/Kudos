package Commands;

import Utils.ComponentCreator;
import Utils.FileManager;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Kudmin implements CommandExecutor, TabCompleter {
    private String prefix;
    private Main plugin;
    private String optionValue;
    private SQLGetter data;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.prefix = "&7» &cKudmin&7: ";
        this.plugin = Main.getPlugin(Main.class);
        this.data = new SQLGetter(plugin);
        FileConfiguration locale = new FileManager("messages.yml", plugin).getConfig();

        if (!sender.hasPermission("kudmin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.no-permission")));
            return false;
        }
        if (args.length == 0) {
            PluginDescriptionFile pluginDescriptionFile = plugin.getDescription();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The plugin is running on version &c" + pluginDescriptionFile.getVersion()));
            return false;
        }

        performAction(sender, args);

        return false;
    }

    private void performAction(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "help" -> sendHelpMessage(sender, args);
            case "reload" -> reloadConfigs(sender, args);
            case "clear" -> performClear(sender, args);
            case "clearall" -> performClearAll(sender, args);
            case "add" -> performAdd(sender, args);
            case "remove" -> performRemove(sender, args);
            case "set" -> performSet(sender, args);
            default -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Unknown argument &e" + args[0] + "&7. Type &e/kudmin help &7to get more informations!"));
        }
    }

    private void sendHelpMessage(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 1, 0, false, false, false))
            return;

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

    private void reloadConfigs(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 1, 0, false, false, false))
            return;
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reloaded configs. A few changes will only take effect after a server restart!"));

        new FileManager("config.yml", plugin).reload();
        new FileManager("messages.yml", plugin).reload();
        new FileManager("mysql.yml", plugin).reload();
        new FileManager("gui.yml", plugin).reload();
    }

    private void performClear(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 2, true, true, false)) {
            return;
        }

        String playerName = args[2];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        switch (optionValue) {
            case "kudos" -> {
                data.clearKudos(playerUUID);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos from &e" + playerName));
            }
            case "assigned_kudos" -> {
                data.clearAssignedKudos(playerUUID);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared assigned Kudos from &e" + playerName));
            }
        }
    }

    private void performClearAll(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 2, 1, false, true, false)) {
            return;
        }

        String playerName = args[1];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        data.clearKudosAndAssignedKudos(playerUUID);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos and assigned Kudos from &e" + playerName));
    }

    private void performSet(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 4, 2, true, true, true)) {
            return;
        }

        int amount = Integer.parseInt(args[3]);
        String playerName = args[2];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        switch (optionValue) {
            case "kudos" -> {
                data.setKudos(playerUUID, amount);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Set &e" + amount + " Kudos &7" + "to &e" + playerName));
            }
            case "assigned_kudos" -> {
                data.setAssignedKudos(playerUUID, amount);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + amount + " assigned Kudos &7" + "to &e" + playerName));
            }
        }
    }

    private void performAdd(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 4, 2, true, true, true))
            return;

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

    private void performRemove(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 4, 2, true, true, true)) {
            return;
        }

        int amount = Integer.parseInt(args[3]);
        String playerName = args[2];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        switch (optionValue) {
            case "kudos" -> {
                if (amount > data.getKudos(playerUUID)) {
                    data.clearKudos(playerUUID);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " Kudos &7" + "from &e" + playerName));
                    return;
                }
                data.removeKudos(playerUUID, amount);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " Kudos &7" + "from &e" + playerName));
            }
            case "assigned_kudos" -> {
                if (amount > data.getAssignedKudo(playerUUID)) {
                    data.clearAssignedKudos(playerUUID);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " assigned Kudos &7" + "from &e" + playerName));
                    return;
                }
                data.removeAssignedKudos(playerUUID, amount);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " assigned Kudos &7" + "from &e" + playerName));
            }
        }
    }

    private boolean validateInput(String[] args, CommandSender sender, int maxArgs, int playerArgumentPosition, boolean validateOptionValue, boolean validateTargetPlayer, boolean validateValue) {
        if (args.length > maxArgs) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. For more informations see &e/kudmin help!"));
            return false;
        }
        if (validateOptionValue && (!setAndValidateOptionValue(sender, args))) return false;
        if (validateTargetPlayer && (!targetPlayerExists(sender, args, playerArgumentPosition))) return false;
        if (validateValue && (!isValueAnInteger(sender, args))) return false;

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

    private boolean targetPlayerExists(CommandSender sender, String[] args, int playerArgumentPosition) {
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
        ArrayList<String> commandArguments = new ArrayList<>();
        List<String> tabCompletions = new ArrayList<>();
        if (!sender.hasPermission("kudmin")) return commandArguments;
        if (args.length == 1) {
            commandArguments.add("help");
            commandArguments.add("add");
            commandArguments.add("remove");
            commandArguments.add("set");
            commandArguments.add("clear");
            commandArguments.add("clearall");
            commandArguments.add("reload");
            StringUtil.copyPartialMatches(args[0], commandArguments, tabCompletions);
        }
        if (args.length == 2) {
            if (args[0].equals("add") || args[0].equals("remove") || args[0].equals("set") || args[0].equals("clear")) {
                commandArguments.add("kudos");
                commandArguments.add("assigned_kudos");
            } else if (args[0].equals("clearall")) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    commandArguments.add(players.getName());
                }
            }
            StringUtil.copyPartialMatches(args[1], commandArguments, tabCompletions);
        }
        if (args.length == 3) {
            if (args[0].equals("add") || args[0].equals("remove") || args[0].equals("set") || args[0].equals("clear")) {
                for (Player players : Bukkit.getOnlinePlayers())
                    commandArguments.add(players.getName());
            }
            StringUtil.copyPartialMatches(args[2], commandArguments, tabCompletions);
        }
        if (args.length == 4) {
            if (args[0].equals("add") || args[0].equals("remove") || args[0].equals("set")) {
                commandArguments.add("amount");
            }
            StringUtil.copyPartialMatches(args[3], commandArguments, tabCompletions);
        }
        return tabCompletions;
    }
}