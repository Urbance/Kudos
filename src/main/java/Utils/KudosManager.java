package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KudosManager {
    Main plugin;
    SQLGetter data;
    FileConfiguration config;
    FileConfiguration locale;
    KudosMessage kudosMessage;

    public KudosManager(Main plugin) {
        this.plugin = plugin;
        this.data = new SQLGetter(plugin);
        this.config = plugin.config;
        this.locale = plugin.localeConfig;
        this.kudosMessage = new KudosMessage(plugin);
    }

    public void addKudo(CommandSender sender, UUID targetPlayerUUID) {
        if (sender instanceof Player) {
            data.addKudos(targetPlayerUUID, ((Player) sender).getUniqueId(), 1);
        } else {
            data.addKudos(targetPlayerUUID, null, 1);
        }
    }

    public boolean addRewards(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("kudo-award.rewards.award-item.enabled")) return true;
        Inventory inventory = targetPlayer.getInventory();
        KudosRewards kudosRewards = new KudosRewards(inventory);

        if (!kudosRewards.addAwardItem()) {
            sendInventoryIsFullMessage(sender, targetPlayer);
            return false;
        }

        kudosRewards.performCommandRewards(targetPlayer);
        return true;
    }

    private void sendInventoryIsFullMessage(CommandSender sender, Player targetPlayer) {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-inventory-is-full"), placeholderValues));
    }

    public void showPlayerKudos(CommandSender sender, OfflinePlayer targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("kudos.show-player-kudos"), values));
    }
}
