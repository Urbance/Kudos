package Commands;

import GUI.OverviewGUI;
import Utils.ConfigManagement;
import Utils.KudosUtils.KudosManagement;
import Utils.KudosUtils.KudosMessage;
import Utils.SQL.SQLGetter;
import Utils.WorkaroundManagement;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class Kudos implements CommandExecutor, TabCompleter {
    private Main plugin = Main.getPlugin(Main.class);
    private FileConfiguration locale;
    private SQLGetter data;
    private OfflinePlayer targetPlayer;
    private KudosManagement kudosManagement;
    private KudosMessage kudosMessage;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (WorkaroundManagement.isLegacyConfig || WorkaroundManagement.isSQLMigrationNeeded || WorkaroundManagement.isConfigMigrationNeeded) {
            return false;
        }

        this.locale = ConfigManagement.getLocalesConfig();
        this.data = new SQLGetter(plugin);
        this.kudosManagement = new KudosManagement();
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
        if (!(sender.hasPermission("kudos.player.gui") || sender.hasPermission("kudos.player.*"))) {
            kudosMessage.noPermission(sender);
            return;
        }
        FileConfiguration overviewConfig = ConfigManagement.getOverviewGuiConfig();
        if (!overviewConfig.getBoolean("general-settings.enabled")) {
            kudosMessage.sendSender(sender, locale.getString("error.specify-player"));
            return;
        }

        Player player = Bukkit.getPlayer(sender.getName());

        OverviewGUI kudosGUI = new OverviewGUI();
        kudosGUI.open(player);
    }

    public void showKudos(CommandSender sender) {
        if (!(sender.hasPermission("kudos.player.show") || sender.hasPermission("kudos.player.*"))) {
            kudosMessage.noPermission(sender);
            return;
        }
        kudosManagement.showPlayerKudos(sender, targetPlayer);
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

        if (!(sender.hasPermission("kudos.player.show") || sender.hasPermission("kudos.player.*"))) return playerNameList;

        if (WorkaroundManagement.isConfigMigrationNeeded) {
            return tabCompletions;
        }

        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNameList.add(player.getName());
            }
            StringUtil.copyPartialMatches(args[0], playerNameList, tabCompletions);
        }
        return tabCompletions;
    }
}
