package Commands;

import Utils.*;
import Utils.KudosUtils.KudosManagement;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kudmin implements CommandExecutor, TabCompleter {
    public static String prefix = "&7» &cKudmin&7: ";
    private Main plugin;
    private SQLGetter data;
    private FileConfiguration config;
    private ValidationManagement validationManagement;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.plugin = Main.getPlugin(Main.class);
        this.data = new SQLGetter(plugin);
        this.config = ConfigManagement.getConfig();
        this.validationManagement = new ValidationManagement();
        FileConfiguration locale = ConfigManagement.getLocalesConfig();

        if (!sender.hasPermission("kudos.admin.*")) {
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
        if (WorkaroundManagement.isSQLMigrationNeeded || WorkaroundManagement.isConfigMigrationNeeded) {
            if (!args[0].equals("migration")) {
                WorkaroundManagement.notifyInstanceAboutWorkaround(sender);
                return;
            }
            performMigration(sender);
            return;
        }

        switch (args[0]) {
            case "help" -> sendHelpMessage(sender, args);
            case "reload" -> reloadConfigs(sender, args);
            case "clear" -> performClear(sender, args);
            case "clearall" -> performClearAll(sender, args);
            case "add" -> performAdd(sender, args);
            case "get" -> performGet(sender, args);
            case "remove" -> performRemove(sender, args);
            default -> sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Unknown argument &e" + args[0] + "&7. Type &e/kudmin help &7to get more informations!"));
        }
    }

    private void performMigration(CommandSender sender) {
        WorkaroundManagement workaroundManagement = new WorkaroundManagement();
        workaroundManagement.performMigrationCheck();

        if (WorkaroundManagement.isConfigMigrationNeeded || WorkaroundManagement.isSQLMigrationNeeded) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Start migration.."));

            if (WorkaroundManagement.isSQLMigrationNeeded) {
                if (!workaroundManagement.performSQLMigration())  {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Something went wrong during SQL table migration. Please check the logs and contact the plugin author."));
                    return;
                }

                workaroundManagement.performMigrationCheck();
                if (WorkaroundManagement.isSQLMigrationNeeded) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error occurred during SQL migration. Please run the migration again or check the console and contact the plugin author."));
                    return;
                }

            }
            if (WorkaroundManagement.isConfigMigrationNeeded) {
                workaroundManagement.performConfigMigration();

                workaroundManagement.performMigrationCheck();
                if (WorkaroundManagement.isConfigMigrationNeeded) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error occurred during config migration. Please run the migration again or check the console and contact the plugin author."));
                    return;
                }
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Performed migration successfully."));
    }

    private void sendHelpMessage(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 1, 0, false, false))
            return;

        String helpText = "&7========= &c&lKudmin Commands &7=========\n" +
                " \n" +
                "&7/kudmin help\n" +
                "&7/kudmin get &e[player] [page]\n" +
                "&7/kudmin add &e[player] [amount] [reason]\n" +
                "&7/kudmin remove &e[player] [kudos_id]\n" +
                "&7/kudmin clear &e[player] [kudos/assigned_kudos]\n" +
                "&7/kudmin clearall &e[player]\n" +
                "&7/kudmin reload\n" +
                " \n" +
                "&7You can find all commands and permissions " ;
        ComponentBuilder helpTextComponent = new ComponentCreator(helpText).createPlainTextComponent(false, null);
        ComponentBuilder wiki = new ComponentCreator("&chere&7!")
                .createLinkTextComponent("&l&o&cClick!","https://urbance.gitbook.io/kudos-v5-wiki/getting-started/permissions-and-commands", false);
        sender.spigot().sendMessage(helpTextComponent
                .append(wiki.create())
                .create());
    }

    private void reloadConfigs(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 1, 0, false, false))
            return;

        String useMySQLValueBeforeReload = config.getString("general-settings.use-MySQL");

        ConfigManagement.reloadAllConfigs(plugin);

        config = ConfigManagement.getConfig();
        String useMySQLValueAfterReload = config.getString("general-settings.use-MySQL");

        String message = "Reloaded configs.";
        if (!useMySQLValueBeforeReload.equals(useMySQLValueAfterReload)) {
            message +=  " You have adjusted the database mode. The change may only take effect after a server restart.";
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));

    }

    private void performClear(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 1, true, false)) return;

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
        int maximumReasonLength = config.getInt("kudo-award.general-settings.reason-length");

        if (!validateInput(args, sender, 3 + maximumReasonLength, 1, true, true)) return;

        if (args.length == 3) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Please define a reason for adding Kudos to that player."));
            return;
        }

        KudosManagement kudosManagement = new KudosManagement();
        String reason = kudosManagement.getReason(args, 4);
        String playerName = args[1];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        int amountKudos = Integer.parseInt(args[2]);

        if (amountKudos > 2500) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "You're trying to add a high amount of Kudos to a player. To avoid server crashes, the maximum number of Kudos that can be added is limited to 2500."));
            return;
        }
        if (reason.length() > maximumReasonLength) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The reason can't be longer than &e" + maximumReasonLength + " &7chars."));
            return;
        }
        if (reason.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "An error has occurred. Can't get reason."));
            return;
        }

        String receivedFromPlayer;
        if (sender instanceof ConsoleCommandSender) {
            receivedFromPlayer = SQLGetter.consoleCommandSenderPrefix;
        } else {
            receivedFromPlayer = String.valueOf(((Player) sender).getUniqueId());
        }

        data.addKudos(playerUUID, receivedFromPlayer, reason, amountKudos);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Added &e" + amountKudos + " Kudos &7" + "to &e" + playerName));
    }

    private void performGet(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 1, true, false)) return;

        String playerName = args[1];
        UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
        List<String> entryKudosList = data.getAllPlayerKudos(playerUUID);

        if (entryKudosList == null || entryKudosList.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The player &e" + playerName + " &7has no Kudos."));
            return;
        }

        int totalEntries = entryKudosList.size();
        int entriesPerPage = 4;
        int maxPages = (int) Math.ceil((double) totalEntries / entriesPerPage);
        int requestedPage = 1;

        if (args.length == 3 && validationManagement.isValueAnIntegerAndGreaterThanZero(args[2])) requestedPage = Integer.parseInt(args[2]);

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
        stringBuilder.append("&7» &cKudos &7from Player &c%player_name% &7[&c%current_page%&7/&c%max_pages%&7]\n");

        for (int entry = startIndex; entry < endIndex; entry++) {
            stringBuilder.append(" \n");
            stringBuilder.append(entryKudosList.get(entry)).append("\n");
        }

        String message = stringBuilder.toString()
                .replace("%max_pages%", String.valueOf(maxPages))
                .replace("%current_page%", String.valueOf(requestedPage))
                .replace("%player_name%", playerName);

       sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void performRemove(CommandSender sender, String[] args) {
        if (!validateInput(args, sender, 3, 1, true, false)) return;

        if (args.length == 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a Kudos ID that you would like to delete. To show Kudos from a player, type &e/kudmin get [player] [page]&7."));
            return;
        }
        if (args.length == 3 && !validationManagement.isValueAnIntegerAndGreaterThanZero(args[2])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Invalid Kudos ID."));
            return;
        }

        String targetPlayerName = args[1];
        int kudoID = data.getPlayerKudo(Integer.parseInt(args[2]));

        if (kudoID == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Kudo with ID &e" + args[2] + " &7not found."));
            return;
        }
        if (!data.removeKudo(kudoID)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Something went wrong while removing the Kudo. Please contact the server administrator."));
            return;
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Removed Kudo with ID &e" + kudoID + " &7from player &e" + targetPlayerName + "&7."));
    }

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
        if (args.length > 2 && (validationManagement.isValueAnIntegerAndGreaterThanZero(args[2]))) return true;

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please enter a positive integer number!"));

        return false;
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
        if (!sender.hasPermission("kudos.admin.*")) return commandArguments;

        if (WorkaroundManagement.isConfigMigrationNeeded || WorkaroundManagement.isSQLMigrationNeeded) {
            if (args.length == 1) commandArguments.add("migration");
            StringUtil.copyPartialMatches(args[0], commandArguments, tabCompletions);
            return commandArguments;
        }

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
                if (args[0].equals("add")) {
                    commandArguments.add("amount");
                }
                if (args[0].equals("remove")) {
                    commandArguments.add("kudos_id");
                }
                if (args[0].equals("clear")) {
                    commandArguments.add("kudos");
                    commandArguments.add("assigned_kudos");
                }
                if (args[0].equals("get")) {
                    commandArguments.add("page");
                }
                StringUtil.copyPartialMatches(args[2], commandArguments, tabCompletions);
            }
            case 4 -> {
                if (args[0].equals("add")) commandArguments.add("reason");
                StringUtil.copyPartialMatches(args[3], commandArguments, tabCompletions);
            }
        }
        return tabCompletions;
    }
}