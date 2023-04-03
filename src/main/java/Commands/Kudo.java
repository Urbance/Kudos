package Commands;

import Utils.*;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Kudo implements CommandExecutor, TabCompleter {
    private Main plugin;
    private KudosMessage kudosMessage;
    private KudosManager kudosManager;
    private FileConfiguration locale;
    private FileConfiguration config;
    private int playerCooldown;
    private final LimitationManager limitationManager = new LimitationManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.plugin = Main.getPlugin(Main.class);
        this.locale = plugin.localeConfig;
        this.config = plugin.config;
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManager = new KudosManager();

        if (!validateInput(args, sender))
            return false;

        awardKudo(sender, args);

        return false;
    }

    private void awardKudo(CommandSender sender, String[] args) {
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        if (sender instanceof Player) this.playerCooldown = plugin.cooldownManager.getCooldown(((Player) sender).getUniqueId());
        if (!playerCanReceiveKudo(sender, targetPlayer)) return;
        if (kudosManager.isMilestone(targetPlayer)) {
            if (!new KudosMilestone().sendMilestone(sender, targetPlayer)) {
                if (sender instanceof Player) plugin.cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
                return;
            }
        } else {
            if (!new KudosAward().sendKudoAward(sender, targetPlayer)) return;
        }

        kudosManager.addKudo(sender, targetPlayerUUID);

        setCooldown(sender);
    }

    private boolean playerCanReceiveKudo(CommandSender sender, Player targetPlayer) {
        if (!validatePlayerCooldown(sender)) return false;
        if (config.getBoolean("kudo-award.limitation.enabled") && !addLimitation(sender, targetPlayer)) return false;

        return true;
    }

    private boolean addLimitation(CommandSender sender, Player targetPlayer) {
        return limitationManager.setLimitation(sender, targetPlayer);
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*"))) {
            kudosMessage.noPermission(sender);
            return false;
        }
        if (args.length == 0) {
            kudosMessage.sendSender(sender, locale.getString("error.specify-player"));
            return false;
        }
        if (args.length > 1) {
            kudosMessage.wrongUsage(sender);
            return false;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_targetplayer_name", args[0]);
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-not-online"), placeholderValues));
            return false;
        }
        if (sender == Bukkit.getPlayer(args[0])) {
            kudosMessage.sendSender(sender, locale.getString("error.cant-give-yourself-kudo"));
            return false;
        }
        return true;
    }

    private void setCooldown(CommandSender sender) {
        if (!(sender instanceof Player))
            return;
        if (config.getInt("kudo-award.cooldown") == 0)
            return;

        UUID senderUUID = ((Player) sender).getUniqueId();
        plugin.cooldownManager.setCooldown(senderUUID, config.getInt("kudo-award.cooldown"));
        new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = plugin.cooldownManager.getCooldown(senderUUID);
                plugin.cooldownManager.setCooldown(senderUUID, --timeLeft);
                if (timeLeft == 0)
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private boolean validatePlayerCooldown(CommandSender sender) {
        if (!canAwardKudos() && sender instanceof Player) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_cooldown", String.valueOf(playerCooldown));
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.must-wait-before-use-again"), placeholderValues));
            return false;
        }
        return true;
    }

    private boolean canAwardKudos() { return playerCooldown == 0; }

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
