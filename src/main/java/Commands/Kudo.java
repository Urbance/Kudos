package Commands;

import Utils.CooldownManager;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Kudo implements CommandExecutor, TabCompleter {
    private final CooldownManager cooldownManager = new CooldownManager();
    public String prefix;
    public SQLGetter data;
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration locale;
    public FileConfiguration config;
    public int timeLeft;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.prefix = plugin.getConfig().getString("prefix");
        this.locale = plugin.locale;
        this.config = plugin.getConfig();

        if (!(sender instanceof Player)) {
            Bukkit.getServer().getLogger().info("You can't execute this command as console!");
            return false;
        }
        if (!validateInput(args, sender))
            return false;

        Player player = ((Player) sender).getPlayer();
        this.timeLeft = cooldownManager.getCooldown(player.getUniqueId());

        if (!canAwardKudos()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +  locale.getString("error.must_wait_before_use_again")
                    .replaceAll("%seconds%", String.valueOf(timeLeft))));
            return false;
        }

        setCooldown(player);

        Player targetPlayer = Bukkit.getPlayer(args[0]);
        data = new SQLGetter(plugin);
        data.addKudos(targetPlayer.getUniqueId(), ((Player) sender).getUniqueId(), 1);

        String awardMessage = locale.getString("kudo.player_award_kudo");
        awardMessage = awardMessage.replaceAll("%player%", player.getName());
        awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
        awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',prefix + awardMessage));

        if (config.getBoolean("play_sound_on_kudo_award")) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                players.playSound(players, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            }
        }

        return false;
    }

    private boolean validateInput(String[] args, CommandSender sender) {
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
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.player_not_online").replaceAll("%targetplayer%", args[0])));
            return false;
        }

        if (sender == Bukkit.getPlayer(args[0])) {
            Bukkit.getPlayer(sender.getName()).sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.cant_give_yourself_kudo")));
            return false;
        }
        return true;
    }

    private void setCooldown(Player player) {
        cooldownManager.setCooldown(player.getUniqueId(), config.getInt("kudo_award_cooldown"));

        new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = cooldownManager.getCooldown(player.getUniqueId());
                cooldownManager.setCooldown(player.getUniqueId(), --timeLeft);
                if (timeLeft == 0)
                    this.cancel();
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    private boolean canAwardKudos() {
        return timeLeft == 0;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*")))
            return list;
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        return list;
    }
}
