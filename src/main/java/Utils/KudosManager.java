package Utils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

    public boolean addItemReward(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("award-item.enabled")) return true;

        Inventory inventory = targetPlayer.getInventory();
        ItemCreator itemCreator = new ItemCreator(Material.getMaterial(config.getString("award-item.item")), config);
        ItemStack awardItem = itemCreator.getItemReward();

        if (!itemCanBeAddedToInventory(awardItem, inventory)) {
            sendInventoryIsFullMessage(sender, targetPlayer);
            return false;
        }

        inventory.addItem(awardItem);
        return true;
    }

    public void showPlayerKudos(CommandSender sender, OfflinePlayer targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));

        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("kudos.show-player-kudos"), values));
    }

    private void sendInventoryIsFullMessage(CommandSender sender, Player targetPlayer) {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-inventory-is-full"), placeholderValues));
    }

    private boolean itemCanBeAddedToInventory(ItemStack itemStack, Inventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                return true;
            }
            if (inventory.getItem(i).isSimilar(itemStack) && !(inventory.getItem(i).getAmount() + config.getInt("award-item.amount") > 64)) {
                return true;
            }
        }
        return false;
    }
}
