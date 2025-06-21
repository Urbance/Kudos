package Utils.KudosUtils;

import Utils.ConfigManagement;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KudosMilestone {
    private Main plugin;
    private FileConfiguration config;
    private SQLGetter data;
    private FileConfiguration locale;
    private KudosMessage kudosMessage;
    private KudosManagement kudosManagement;

    public KudosMilestone() {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.config = ConfigManagement.getConfig();
        this.data = new SQLGetter(plugin);
        this.locale = ConfigManagement.getLocalesConfig();
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManagement = new KudosManagement();
    }

    public boolean sendMilestone(CommandSender sender, Player targetPlayer, String reason) {
        if (!addRewards(sender, targetPlayer)) {
            return false;
        }
        if (!kudosManagement.addKudo(sender, targetPlayer.getUniqueId(), reason)) {
            return false;
        }

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        placeholderValues.put("kudos_targetplayer_kudos", String.valueOf(data.getAmountKudos(targetPlayer.getUniqueId()) + 1));
        if (reason != null) placeholderValues.put("kudos_award_reason", reason);
        String milestonesMessage = locale.getString("error.something-went-wrong-by-sending-milestone-broadcast");

        if (sender instanceof ConsoleCommandSender) {
            if (reason == null) {
                milestonesMessage = kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone-through-console"), placeholderValues);
            } else {
                milestonesMessage = kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone-through-console-with-reason"), placeholderValues);
            }
        }
        if (sender instanceof Player) {
            placeholderValues.put("kudos_player_name", sender.getName());
            if (reason == null) {
                milestonesMessage = kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone"), placeholderValues);
            } else {
                milestonesMessage = kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone-with-reason"), placeholderValues);
            }
        }

        if (!Objects.equals(milestonesMessage, locale.getString("error.something-went-wrong-by-sending-milestone-broadcast")) || milestonesMessage != null) {
            kudosMessage.broadcast(milestonesMessage);
            playMilestoneSound(sender, targetPlayer);
            return true;
        }

        kudosMessage.sendSender(sender, milestonesMessage);
        Bukkit.getLogger().warning(plugin.prefix + "Something went wrong by sending the milestone broadcast during a milestone for player " + targetPlayer + " from player " + sender.getName() + "!");

        return false;
    }

    private boolean addRewards(CommandSender sender, Player targetPlayer) {
        if (!kudosManagement.addItemRewards(sender, targetPlayer, "kudo-award.milestones.rewards.items")) return false;

        new KudosManagement().performCommandRewards(KudosManagement.AwardType.MILESTONE, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.milestones.rewards.xp"));
        return true;
    }

    private void playMilestoneSound(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("kudo-award.milestones.playsound.enabled"))
            return;
        kudosManagement.playSound(sender, targetPlayer, config.getString("kudo-award.milestones.playsound.playsound-type"));
    }
}
