package Commands;

import Utils.KudosUtils.KudosGUI;
import Utils.KudosUtils.KudosManager;
import Utils.KudosUtils.KudosMessage;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.StringUtil;

import java.util.*;

public class Kudos implements CommandExecutor, TabCompleter {
    public static Inventory inventory;
    Main plugin = Main.getPlugin(Main.class);
    FileConfiguration locale;
    SQLGetter data;
    String prefix;
    OfflinePlayer targetPlayer;
    KudosManager kudosManager;
    KudosMessage kudosMessage;


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.prefix = plugin.prefix;
        this.locale = plugin.localeConfig;
        this.data = new SQLGetter(plugin);
        this.kudosManager = new KudosManager();
        this.kudosMessage = new KudosMessage(plugin);

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
            kudosMessage.noPermission(sender);
            return;
        }

        Player player = Bukkit.getPlayer(sender.getName());
        inventory = new KudosGUI().create(player);
        player.openInventory(inventory);
    }

    public void showKudos(CommandSender sender) {
        if (!(sender.hasPermission("kudos.show") || sender.hasPermission("kudos.*"))) {
            kudosMessage.noPermission(sender);
            return;
        }
        kudosManager.showPlayerKudos(sender, targetPlayer);
}

    public boolean validateInput(String[] args, CommandSender sender) {
        if (args.length > 1) {
            kudosMessage.wrongUsage(sender);
            return false;
        }
        if (args.length == 1) {
            targetPlayer = data.getPlayer(Bukkit.getOfflinePlayer(args[0]).getUniqueId());
            if (targetPlayer == null) {
                Map<String, String> values = new HashMap<>();
                values.put("kudos_targetplayer_name", args[0]);
                kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-not-found"), values));
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> playerNameList = new ArrayList<>();
        List<String> tabCompletions = new ArrayList<>();
        if (!(sender.hasPermission("kudos.show") || sender.hasPermission("kudos.*"))) return playerNameList;
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNameList.add(player.getName());
            }
            StringUtil.copyPartialMatches(args[0], playerNameList, tabCompletions);
        }
        return tabCompletions;
    }
}
