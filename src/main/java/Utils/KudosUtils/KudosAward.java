package Utils.KudosUtils;

import de.urbance.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KudosAward {
    private Main plugin;
    private FileConfiguration config;
    private KudosManager kudosManager;

    public KudosAward() {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.config = plugin.config;
        this.kudosManager = new KudosManager();
    }

    public boolean sendKudoAward(CommandSender sender, Player targetPlayer) {
        if (!addRewards(sender, targetPlayer)) {
             if (sender instanceof Player) plugin.cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
            return false;
        }
        sendNotification(sender, targetPlayer);
        return true;
    }

    private void sendNotification(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("kudo-award.notification.enabled")) return;

        KudosNotification kudosNotification = new KudosNotification();
        String notificationMode = kudosManager.getNotificationMode();
        playNotificationSound(sender, targetPlayer, notificationMode);

        if (!(sender instanceof Player)) {
            kudosNotification.fromConsole(targetPlayer);
            return;
        }
        if (notificationMode.equals("broadcast")) {
            kudosNotification.sendBroadcastMessage(sender, targetPlayer);
            return;
        }
        if (notificationMode.equals("private")) {
            kudosNotification.sendPrivate(sender, targetPlayer);
        }
    }

    private boolean addRewards(CommandSender sender, Player targetPlayer) {
        if (!kudosManager.addItemRewards(sender, targetPlayer, "kudo-award.rewards.items")) return false;

        kudosManager.performCommandRewards(KudosManager.AwardType.AWARD, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.rewards.xp"));
        return true;
    }

    private void playNotificationSound(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (!config.getBoolean("kudo-award.notification.enable-playsound")) return;
        if (notificationMode.equals("private") || notificationMode.equals("broadcast"))
            kudosManager.playSound(sender, targetPlayer, config.getString("kudo-award.notification.playsound-type"));
    }
}
