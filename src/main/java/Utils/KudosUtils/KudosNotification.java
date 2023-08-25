package Utils.KudosUtils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KudosNotification {
    private KudosMessage kudosMessage;
    private SQLGetter data;
    private FileConfiguration locale;
    private KudosManagement kudosManagement;
    private boolean awardReasonsAreEnabled;

    public KudosNotification() {
        Main plugin = JavaPlugin.getPlugin(Main.class);
        this.kudosMessage = new KudosMessage(plugin);
        this.data = new SQLGetter(plugin);
        this.locale = plugin.localeConfig;
        this.kudosManagement = new KudosManagement();
        this.awardReasonsAreEnabled = plugin.config.getBoolean("kudo-award.enable-reasons");
    }

    public void fromConsole(Player targetPlayer, String reason) {
        UUID targetPlayerUUID = targetPlayer.getUniqueId();
        Map<String, String> values = new HashMap<>();
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getAmountKudos(targetPlayerUUID) + 1));

        String awardMessage = kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-from-console"), values);

        if (reason != null) {
            values.put("kudos_award_reason", reason);
            awardMessage = kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-from-console-with-reason"), values);
        }
        kudosMessage.broadcast(awardMessage);
    }

    public void sendBroadcastMessage(CommandSender sender, Player targetPlayer, String reason) {
        UUID targetPlayerUUID = targetPlayer.getUniqueId();
        Map<String, String> values = new HashMap<>();
        values.put("kudos_player_name", sender.getName());
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getAmountKudos(targetPlayerUUID) + 1));

        String awardMessage = kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-broadcast"), values);

        if (reason != null) {
            values.put("kudos_award_reason", reason);
            awardMessage = kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-broadcast-with-reason"), values);
        }
        kudosMessage.broadcast(awardMessage);
    }

    public void sendPrivate(CommandSender sender, Player targetPlayer) {
        sendPrivateMessageToSender(sender, targetPlayer);
        sendPrivateMessageToTargetPlayer(sender, targetPlayer);
    }

    private void sendPrivateMessageToSender(CommandSender sender, Player targetPlayer) {
        Map<String, String> valuesSender = new HashMap<>();
        valuesSender.put("kudos_targetplayer_name", targetPlayer.getName());
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("kudo.player-assigned-kudo"), valuesSender));
    }

    private void sendPrivateMessageToTargetPlayer(CommandSender sender, Player targetPlayer) {
        Map<String, String> valuesTargetPlayer = new HashMap<>();
        valuesTargetPlayer.put("kudos_player_name", sender.getName());
        valuesTargetPlayer.put("kudos_player_kudos", String.valueOf(data.getAmountKudos(targetPlayer.getUniqueId()) + 1));
        kudosMessage.send(targetPlayer, kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-from-player"), valuesTargetPlayer));
    }

}
