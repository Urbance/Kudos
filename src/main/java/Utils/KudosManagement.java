package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

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

    public void addKudo() {
    }

    public void showKudos(Player player, OfflinePlayer targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("targetplayer", targetPlayer.getName());
        values.put("targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        kudosMessage.send(player, kudosMessage.setPlaceholders(locale.getString("kudos.show-player-kudos"), values));
    }
}
