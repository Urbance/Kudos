package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KudosNotification {
    Main plugin;
    String prefix;
    KudosMessage kudosMessage;
    SQLGetter data;
    FileConfiguration locale;

    public KudosNotification(Main plugin) {
        this.plugin = plugin;
        this.prefix = plugin.prefix;
        this.kudosMessage = new KudosMessage(plugin);
        this.data = new SQLGetter(plugin);
        this.locale = plugin.localeConfig;
    }

    public void fromConsole(Player targetPlayer) {
        UUID targetPlayerUUID = targetPlayer.getUniqueId();
        Map<String, String> values = new HashMap<>();
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayerUUID)));
        kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-from-console"), values));
    }

    public void sendBroadcast(CommandSender sender, Player targetPlayer) {
        UUID targetPlayerUUID = targetPlayer.getUniqueId();
        Map<String, String> values = new HashMap<>();
        values.put("kudos_player_name", sender.getName());
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayerUUID)));
        kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-broadcast"), values));
    }

    public void sendPrivate(CommandSender sender, Player targetPlayer) {
        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        // send message to sender
        Map<String, String> valuesSender = new HashMap<>();
        valuesSender.put("kudos_targetplayer_name", targetPlayer.getName());
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("kudo.player-assigned-kudo"), valuesSender));

        // send message to target player
        Map<String, String> valuesTargetPlayer = new HashMap<>();
        valuesTargetPlayer.put("kudos_player_name", sender.getName());
        valuesTargetPlayer.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayerUUID)));
        kudosMessage.send(targetPlayer, kudosMessage.setPlaceholders(locale.getString("kudo.player-award-kudo-from-player"), valuesTargetPlayer));
    }

}
