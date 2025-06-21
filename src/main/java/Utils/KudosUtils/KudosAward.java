package Utils.KudosUtils;

import Utils.ConfigManagement;
import de.urbance.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KudosAward {
    private FileConfiguration config;
    private KudosManagement kudosManagement;

    public KudosAward() {
        this.config = ConfigManagement.getConfig();
        this.kudosManagement = new KudosManagement();
    }

    public boolean sendKudoAward(CommandSender sender, Player targetPlayer, String reason) {
        if (!addRewards(sender, targetPlayer)) {
            return false;
        }
        if (!kudosManagement.addKudo(sender, targetPlayer.getUniqueId(), reason)) {
            return false;
        }
        sendNotification(sender, targetPlayer, reason);
        return true;
    }

    private void sendNotification(CommandSender sender, Player targetPlayer, String reason) {
        if (!config.getBoolean("kudo-award.notification.enabled")) return;

        KudosNotification kudosNotification = new KudosNotification();
        String notificationMode = kudosManagement.getNotificationMode();
        playNotificationSound(sender, targetPlayer, notificationMode);

        if (sender instanceof ConsoleCommandSender) {
            kudosNotification.fromConsole(targetPlayer, reason);
            return;
        }
        switch (notificationMode) {
            case "broadcast" -> kudosNotification.sendBroadcastMessage(sender, targetPlayer, reason);
            case "private" -> kudosNotification.sendPrivate(sender, targetPlayer, reason);
        }
    }

    private boolean addRewards(CommandSender sender, Player targetPlayer) {
        if (!kudosManagement.addItemRewards(sender, targetPlayer, "kudo-award.rewards.items")) return false;

        kudosManagement.performCommandRewards(KudosManagement.AwardType.AWARD, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.rewards.xp"));
        return true;
    }

    private void playNotificationSound(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (!config.getBoolean("kudo-award.notification.enable-playsound")) return;
        if (notificationMode.equals("private") || notificationMode.equals("broadcast"))
            kudosManagement.playSound(sender, targetPlayer, config.getString("kudo-award.notification.playsound-type"));
    }
}
