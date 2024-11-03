package Commands;

import Utils.KudosUtils.*;
import Utils.WorkaroundManagement;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import java.util.*;

public class Kudo implements CommandExecutor, TabCompleter {
    private Main plugin;
    private KudosMessage kudosMessage;
    private KudosManagement kudosManagement;
    private FileConfiguration locale;
    private FileConfiguration config;
    private int playerCooldown;
    private final KudosLimitation kudosLimitation = new KudosLimitation();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (WorkaroundManagement.isLegacyConfig || WorkaroundManagement.isSQLMigrationNeeded || WorkaroundManagement.isConfigMigrationNeeded) {
            return false;
        }

        this.plugin = Main.getPlugin(Main.class);
        this.locale = plugin.localeConfig;
        this.config = plugin.config;
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManagement = new KudosManagement();
        String reason = null;
        if (config.getBoolean("kudo-award.general-settings.enable-reasons") && args.length > 1) reason = kudosManagement.getReason(args, 2);
        if (!validateInput(args, sender, reason)) return false;

        awardKudo(sender, args, reason);
        return false;
    }

    private void awardKudo(CommandSender sender, String[] args, String reason) {
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        if (sender instanceof Player) this.playerCooldown = plugin.cooldownManager.getCooldown(((Player) sender).getUniqueId());
        if (!playerCanReceiveKudo(sender, targetPlayer)) return;
        if (kudosManagement.isMilestone(targetPlayer)) {
            if (!new KudosMilestone().sendMilestone(sender, targetPlayer, reason)) {
                if (sender instanceof Player) plugin.cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
                return;
            }
        } else {
            if (!new KudosAward().sendKudoAward(sender, targetPlayer, reason)) return;
        }
        kudosManagement.addKudo(sender, targetPlayerUUID, reason);
        setCooldown(sender);
    }

    private boolean playerCanReceiveKudo(CommandSender sender, Player targetPlayer) {
        if (!validatePlayerCooldown(sender)) return false;
        if (config.getBoolean("kudo-award.limitation.enabled") && !kudosLimitation.setLimitation(sender, targetPlayer)) return false;
        return true;
    }

    private boolean validateInput(String[] args, CommandSender sender, String reason) {
        if (!(sender.hasPermission("kudos.player.award") || sender.hasPermission("kudos.player.*"))) {
            kudosMessage.noPermission(sender);
            return false;
        }
        if (args.length == 0) {
            kudosMessage.sendSender(sender, locale.getString("error.specify-player"));
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
        if (args.length > 1) {
            if (config.getBoolean("kudo-award.general-settings.enable-reasons")) {
                int maximumReasonLength = config.getInt("kudo-award.general-settings.reason-length");
                if (reason.length() < maximumReasonLength) {
                    return true;
                }
                kudosMessage.sendSender(sender, locale.getString("error.award-reason-is-too-long").replace("%kudos_maximum_award_reason_chars%", String.valueOf(maximumReasonLength)));
                return false;
            }
            kudosMessage.wrongUsage(sender);
            return false;
        }
        return true;
    }

    private void setCooldown(CommandSender sender) {
        if (!(sender instanceof Player))
            return;
        if (config.getInt("kudo-award.general-settings.cooldown") == 0)
            return;

        UUID senderUUID = ((Player) sender).getUniqueId();
        plugin.cooldownManager.setCooldown(senderUUID, config.getInt("kudo-award.general-settings.cooldown"));
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
        if (playerCooldown > 0 && sender instanceof Player) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_cooldown", String.valueOf(playerCooldown));
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.must-wait-before-use-again"), placeholderValues));
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> commandArguments = new ArrayList<>();
        List<String> tabCompletions = new ArrayList<>();
        FileConfiguration config = Main.getPlugin(Main.class).config;

        if (!(sender.hasPermission("kudos.player.award") || sender.hasPermission("kudos.player.*"))) return commandArguments;

        if (WorkaroundManagement.isConfigMigrationNeeded) {
            return tabCompletions;
        }

        switch (args.length) {
            case 1 -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    commandArguments.add(player.getName());
                }
                StringUtil.copyPartialMatches(args[0], commandArguments, tabCompletions);
            }
            case 2 -> {
                if (config.getBoolean("kudo-award.general-settings.enable-reasons")) {
                    tabCompletions.add("reason");
                }
                StringUtil.copyPartialMatches(args[1], commandArguments, tabCompletions);
            }
        }
        return tabCompletions;
    }
}
