package Commands;

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

import static de.urbance.Main.prefix;

public class Kudo implements CommandExecutor {
    public SQLGetter data;
    FileConfiguration locale;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Bukkit.getServer().getLogger().info("You can't execute this command as console!");
            return false;
        }

        if (!validateInput(args, sender))
            return false;

        Player player = ((Player) sender).getPlayer();
        locale = new LocaleManager(Main.getPlugin(Main.class)).getConfig();

        Player targetPlayer = Bukkit.getPlayer(args[0]);

        data = new SQLGetter(Main.getPlugin(Main.class));
        data.addKudos(targetPlayer.getUniqueId(), 1);

        String awardMessage = locale.getString("kudo.player_award_kudo");
        awardMessage = awardMessage.replaceAll("%player%", player.getName());
        awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
        awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',prefix + awardMessage));

        return false;
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        locale = new LocaleManager(Main.getPlugin(Main.class)).getConfig();

        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*"))) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.no_permission")));
            return false;
        }

        if (args.length == 0) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.specify_player")));
            return false;
        }

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

        if (sender == Bukkit.getPlayer(args[0])) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.cant_give_yourself_kudo")));
            return false;
        }
        return true;
    }

}