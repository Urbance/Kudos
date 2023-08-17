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
import java.util.List;
import java.util.UUID;

public class Kudmin implements CommandExecutor, TabCompleter {
    private String prefix;
    private Main plugin;
    private SQLGetter data;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.prefix = "&7» &cKudmin&7: ";
        this.plugin = Main.getPlugin(Main.class);
        this.data = new SQLGetter(plugin);
        FileConfiguration locale = new FileManager("messages.yml", plugin).getConfig();

        if (!sender.hasPermission("kudos.kudmin.*")) {
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
            case "get" -> performGet(sender, args);
           // case "remove" -> performRemove(sender, args);
            default -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Unknown argument &e" + args[0] + "&7. Type &e/kudmin help &7to get more informations!"));
        }
    }

    private void sendHelpMessage(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 1, 0, false, false))
            return;

        String helpText = "&7========= &c&lKudmin Commands &7=========\n" +
                " \n" +
                "&7/kudmin help\n" +
                "&7/kudmin add &ekudos [player] [amount]\n" +
                "&7/kudmin get &e[kudos/assigned_kudos] [player] [page]\n" +
                "&7/kudmin remove &ekudos [player] [amount]\n" +
                "&7/kudmin clear &e[kudos/assigned_kudos] [player]\n" +
                "&7/kudmin clearall &e[player]\n" +
                "&7/kudmin reload\n" +
                " \n" +
                "&7You can find all commands and permissions " ;
        ComponentBuilder helpTextComponent = new ComponentCreator(helpText).createPlainTextComponent(false, null);
        ComponentBuilder wiki = new ComponentCreator("&chere&7!")
                .createLinkTextComponent("&l&o&cClick!","https://urbance.gitbook.io/kudos-v4-wiki/getting-started/permissions-and-commands", false);
        sender.spigot().sendMessage(helpTextComponent
                .append(wiki.create())
                .create());
    }

    private void reloadConfigs(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 1, 0, false, false))
            return;
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reloaded configs. A few changes will only take effect after a server restart!"));

        new FileManager("config.yml", plugin).reload();
        new FileManager("messages.yml", plugin).reload();
        new FileManager("mysql.yml", plugin).reload();
        new FileManager("gui.yml", plugin).reload();
    }

    private void performClear(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 1, true, false)) {
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. Please choose between &ekudos &7or &eassigned_kudos&7!"));
            return;
        }
        String optionValue = args[2];
        if (!(optionValue.equals("kudos") || optionValue.equals("assigned_kudos"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. Please choose between &ekudos &7or &eassigned_kudos&7!"));
            return;
        }

        String playerName = args[1];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        switch (optionValue) {
            case "kudos" -> {
                if (!data.clearKudos(playerUUID)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error has occurred! Please contact the system administrator or the developer of the plugin."));
                    return;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos from &e" + playerName));
            }
            case "assigned_kudos" -> {
                if (!data.clearAssignedKudos(playerUUID)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error has occurred! Please contact the system administrator or the developer of the plugin."));
                    return;
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared assigned Kudos from &e" + playerName));
            }
        }
    }

    private void performClearAll(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 2, 1, true, false)) {
            return;
        }

        String playerName = args[1];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();

        if (!data.clearKudosAndAssignedKudos(playerUUID)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error has occurred! Please contact the system administrator or the developer of the plugin."));
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Cleared Kudos and assigned Kudos from &e" + playerName));
    }

    private void performAdd(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 1, true, true))
            return;

        String playerName = args[1];
        UUID player = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        int amountKudos = Integer.parseInt(args[2]);

        data.addKudos(player, "SYSTEM", null, amountKudos);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + amountKudos + " Kudos &7" + "to &e" + playerName));
    }

    private void performGet(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 1, true, false)) return;

        String playerName = args[1];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        List<String> entryKudosList = data.getPlayerKudos(playerUUID);

        if (entryKudosList.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The player &e" + playerName + " &7has no Kudos."));
            return;
        }

        int totalEntries = entryKudosList.size();
        int entriesPerPage = 5;
        int maxPages = (int) Math.ceil((double) totalEntries / entriesPerPage);
        int requestedPage = 1;

        if (args.length == 3 && isValueAnInteger(args[2])) requestedPage = Integer.parseInt(args[2]);

        if (requestedPage > maxPages || requestedPage == 0) {
            if (requestedPage == 0 && maxPages == 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Please enter a page number of at least &e1&7."));
                return;
            }
            if (requestedPage > 1 && maxPages == 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "There is no more than one page."));
                return;
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Please enter a number between &e1" + " &7and &e" + maxPages + "&7."));
            return;
        }

        int startIndex = (requestedPage - 1) * entriesPerPage;
        int endIndex = Math.min(startIndex + entriesPerPage, totalEntries);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("&7========= &c&l Kudos from Player %player_name% [%current_page%/%max_pages%] &7=========\n");
        stringBuilder.append("&aID &7| &aFrom Player &7| &aReason &7| &aReceived At\n");

        for (int entry = startIndex; entry < endIndex; entry++) {
            stringBuilder.append(entryKudosList.get(entry)).append("\n");
        }

        String message = stringBuilder.toString()
                .replace("%max_pages%", String.valueOf(maxPages))
                .replace("%current_page%", String.valueOf(requestedPage))
                .replace("%player_name%", playerName);

       sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

//    private void performRemove(CommandSender sender, String[] args) {
//        // TODO test validateValue 0
//        if (!validateInput(args, sender, 4, 3, true, true)) {
//            return;
//        }
//
//        String optionValue = args[1];
//        String playerName = args[2];
//        int id = Integer.parseInt(args[3]);
//        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
//
//
//        switch (optionValue) {
//            case "kudos" -> {
//                if (amount > data.getKudos(playerUUID)) {
//                    if (!data.clearKudos(playerUUID)) {
//                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error has occurred! Please contact the system administrator or the developer of this plugin."));
//                        return;
//                    }
//                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " Kudos &7" + "from &e" + playerName));
//                    return;
//                }
//                data.removeKudos(playerUUID, amount);
//                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Removed &e" + amount + " Kudos &7" + "from &e" + playerName));
//            }
//        }
//    }

    private boolean validateInput(String[] args, CommandSender sender, int maxArgs, int playerArgumentPosition, boolean validateTargetPlayer, boolean validateValue) {
        if (args.length > maxArgs) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Wrong usage. For more informations see &e/kudmin help!"));
            return false;
        }
        if (validateTargetPlayer && (!targetPlayerExists(sender, args, playerArgumentPosition))) return false;
        if (validateValue && (!checkIfKudminValueIsValid(sender, args))) return false;

        return true;
    }

    private boolean checkIfKudminValueIsValid(CommandSender sender, String[] args) {
        if (args.length < 3 || !isValueAnInteger(args[2]) || Integer.parseInt(args[2]) < 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a positive integer number!"));
            return false;
        }
        return true;
    }

    private boolean isValueAnInteger(String value) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
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
        if (!sender.hasPermission("kudos.kudmin.*")) return commandArguments;

        switch (args.length) {
            case 1 -> {
                commandArguments.add("help");
                commandArguments.add("add");
                commandArguments.add("remove");
                commandArguments.add("get");
                commandArguments.add("clear");
                commandArguments.add("clearall");
                commandArguments.add("reload");
                StringUtil.copyPartialMatches(args[0], commandArguments, tabCompletions);
            }
            case 2 -> {
                if (args[0].equals("add") || args[0].equals("remove") || args[0].equals("clear") || args[0].equals("clearall") || args[0].equals("get")) {
                    for (Player players : Bukkit.getOnlinePlayers()) {
                        commandArguments.add(players.getName());
                    }
                }
                StringUtil.copyPartialMatches(args[1], commandArguments, tabCompletions);
            }
            case 3 -> {
                if (args[0].equals("add") || args[0].equals("remove")) {
                    commandArguments.add("amount");
                }
                if (args[0].equals("clear")) {
                    commandArguments.add("kudos");
                    commandArguments.add("assigned_kudos");
                }
                if (args[0].equals("get")) {
                    commandArguments.add("site");
                }
                StringUtil.copyPartialMatches(args[2], commandArguments, tabCompletions);
            }
        }
        return tabCompletions;
    }
}