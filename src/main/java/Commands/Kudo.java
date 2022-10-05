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

import java.io.File;

public class Kudo implements CommandExecutor {
    public SQLGetter data;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();

            if (player.hasPermission("kudos.award") || player.hasPermission("kudos.*")) {
                String prefix = Main.prefix;

                if (args.length == 0) {
                    Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Please specify a player!"));
                }

                if (args.length == 1) {
                    Player targetPlayer = Bukkit.getPlayer(args[0]);
                    FileConfiguration locale = new LocaleManager(Main.getPlugin(Main.class)).getConfig();

                    if (player == targetPlayer) {
                        Bukkit.getPlayer(player.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "You can't give yourself a kudo!"));
                        return false;
                    }
                    data = new SQLGetter(Main.getPlugin(Main.class));
                    data.addKudos(targetPlayer.getUniqueId(), 1);

                    String awardMessage = locale.getString("messages.player_award_kudo");
                    awardMessage = awardMessage.replaceAll("%player%", player.getName());
                    awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
                    awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',prefix + awardMessage));
                }
            }

        } else {
            Bukkit.getServer().getLogger().info("You can't execute this command as console!");
        }
        return false;
    }
}
