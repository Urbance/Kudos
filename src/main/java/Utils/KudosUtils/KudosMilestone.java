package Utils.KudosUtils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class KudosMilestone {
    private Main plugin;
    private FileConfiguration config;
    private SQLGetter data;
    private FileConfiguration locale;
    private KudosMessage kudosMessage;
    private KudosManager kudosManager;

    public KudosMilestone() {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.config = plugin.config;
        this.data = new SQLGetter(plugin);
        this.locale = plugin.localeConfig;
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManager = new KudosManager();
    }

    public boolean sendMilestone(CommandSender sender, Player targetPlayer) {
        if (!addRewards(sender, targetPlayer)) {
            if (sender instanceof Player) plugin.cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
            return false;
        }

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        placeholderValues.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId()) + 1));

        if (sender instanceof ConsoleCommandSender) kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone-through-console"), placeholderValues));
        if (sender instanceof Player) {
            placeholderValues.put("kudos_player_name", sender.getName());
            kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone"), placeholderValues));
        }
        playMilestoneSound(sender, targetPlayer);
        return true;
    }

    private boolean addRewards(CommandSender sender, Player targetPlayer) {
        if (!kudosManager.addItemRewards(sender, targetPlayer, "kudo-award.milestones.rewards.items")) return false;

        new KudosManager().performCommandRewards(KudosManager.AwardType.MILESTONE, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.milestones.rewards.xp"));
        return true;
    }

    private void playMilestoneSound(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("kudo-award.milestones.enable-playsound"))
            return;
        kudosManager.playSound(sender, targetPlayer, config.getString("kudo-award.milestones.playsound-type"));
    }
}
