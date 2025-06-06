package Commands;

import Utils.ConfigManagement;
import Utils.KudosUtils.*;
import Utils.WorkaroundManagement;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Kudo implements CommandExecutor, TabCompleter {
    private Main plugin;
    private KudosMessage kudosMessage;
    private KudosManagement kudosManagement;
    private FileConfiguration locale;
    private FileConfiguration config;
    private final KudosLimitation kudosLimitation = new KudosLimitation();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (WorkaroundManagement.isLegacyConfig || WorkaroundManagement.isSQLMigrationNeeded || WorkaroundManagement.isConfigMigrationNeeded) {
            return false;
        }

        this.plugin = Main.getPlugin(Main.class);
        this.locale = ConfigManagement.getLocalesConfig();
        this.config = ConfigManagement.getConfig();
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManagement = new KudosManagement();
        String reason = null;
        if (config.getBoolean("kudo-award.general-settings.enable-reasons") && args.length > 1)
            reason = kudosManagement.getReason(args, 2);
        if (!validateInput(args, sender, reason)) return false;

        awardKudo(sender, args, reason);
        return false;
    }

    private void awardKudo(CommandSender sender, String[] args, String reason) {
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        if (!playerCanReceiveKudo(sender, targetPlayer)) return;
        if (kudosManagement.isMilestone(targetPlayer)) {
            if (!new KudosMilestone().sendMilestone(sender, targetPlayer, reason)) {
                return;
            }
        } else {
            if (!new KudosAward().sendKudoAward(sender, targetPlayer, reason)) return;
        }
        kudosManagement.addKudo(sender, targetPlayerUUID, reason);
    }

    private boolean playerCanReceiveKudo(CommandSender sender, Player targetPlayer) {
        if (!validatePlayerCooldown(sender)) return false;
        return !config.getBoolean("kudo-award.limitation.enabled") || kudosLimitation.setLimitation(sender, targetPlayer);
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

    private boolean validatePlayerCooldown(CommandSender sender) {
        if (!(sender instanceof Player))
            return true;

        UUID senderUUID = ((Player) sender).getUniqueId();

        String stringLastKudoAwardedAt = plugin.data.getLastKudoAwardedDateFromPlayer(senderUUID);

        if (stringLastKudoAwardedAt == null || stringLastKudoAwardedAt.isBlank())
            return true;

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        LocalDateTime lastKudoAwardedAt = LocalDateTime.parse(stringLastKudoAwardedAt, dateFormat);
        LocalDateTime nextKudoCanAwardedAt = lastKudoAwardedAt.plusSeconds(config.getLong("kudo-award.general-settings.cooldown"));

        Long secondsUntilNextKudoCanAwarded = Duration.between(LocalDateTime.now(), nextKudoCanAwardedAt).getSeconds();

        if (secondsUntilNextKudoCanAwarded > 0) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_cooldown", String.valueOf(secondsUntilNextKudoCanAwarded));
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

        if (!(sender.hasPermission("kudos.player.award") || sender.hasPermission("kudos.player.*")))
            return commandArguments;

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
