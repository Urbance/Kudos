package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class KudosManagement {
    Main plugin;
    SQLGetter data;
    FileConfiguration locale;
    KudosMessage kudosMessage;

    public KudosManagement(Main plugin) {
        this.plugin = plugin;
        this.data = new SQLGetter(plugin);
        this.locale = plugin.localeConfig;
        this.kudosMessage = new KudosMessage(plugin);
    }

    public void showKudos(CommandSender sender, OfflinePlayer targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("targetplayer", targetPlayer.getName());
        values.put("targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("kudos.show-player-kudos"), values));
    }
}
