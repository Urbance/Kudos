package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

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
        if (!config.getBoolean("kudo-award.rewards.award-item.enabled")) return true;
        if (!addAwardItemAndPerformCommandRewards(targetPlayer)) {
            kudosManager.sendInventoryIsFullMessage(sender, targetPlayer);
            return false;
        }
        return true;
    }

    private boolean addAwardItemAndPerformCommandRewards(Player targetPlayer) {
        ItemCreator itemCreator = new ItemCreator(Material.getMaterial(config.getString("kudo-award.rewards.award-item.item")));
        ItemStack awardItem = itemCreator.getItemReward();
        Inventory inventory = targetPlayer.getInventory();
        if (!kudosManager.itemCanBeAddedToInventory(awardItem, inventory)) return false;

        inventory.addItem(awardItem);
        performCommandRewards(targetPlayer);
        return true;
    }

    public void performCommandRewards(Player targetplayer) {
        if (!config.getBoolean("kudo-award.rewards.command-rewards.enabled"))
            return;

        for (String commands : config.getStringList("kudo-award.rewards.command-rewards.commands")) {
            Map<String, String> values = new HashMap<>();
            values.put("kudos_player_name", targetplayer.getName());
            String command = new KudosMessage(plugin).setPlaceholders(commands, values);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private void playNotificationSound(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (!config.getBoolean("kudo-award.notification.enable-playsound")) return;
        if (notificationMode.equals("private") || notificationMode.equals("broadcast"))
            kudosManager.playSound(sender, targetPlayer, config.getString("kudo-award.notification.playsound-type"));
    }
}
