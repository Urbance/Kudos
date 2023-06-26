package Utils.KudosUtils;

import Utils.ItemCreator;
import de.urbance.Main;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
        // if (!config.getBoolean("kudo-award.rewards.award-item.enabled")) return true;
        if (!addAwardItemAndPerformRewards(targetPlayer)) {
            kudosManager.sendInventoryIsFullMessage(sender, targetPlayer);
            return false;
        }
        return true;
    }

    private boolean addAwardItemAndPerformRewards(Player targetPlayer) {
        Inventory inventory = targetPlayer.getInventory();

        // if (!kudosManager.itemCanBeAddedToInventory(itemStacks, inventory)) return false;

        if (!addRewardItems(inventory, targetPlayer)) return false;

        new KudosManager().performCommandRewards(KudosManager.AwardType.AWARD, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.rewards.xp"));
        return true;
    }

    private boolean addRewardItems(Inventory inventory, Player targetPlayer) {
        ArrayList<ItemStack> itemStackList = getItemRewards();
        if (!kudosManager.itemCanBeAddedToInventory(itemStackList, inventory)) return false;
        for (ItemStack itemStack : itemStackList) {
            inventory.addItem(itemStack);
        }
        return true;
    }

    private void playNotificationSound(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (!config.getBoolean("kudo-award.notification.enable-playsound")) return;
        if (notificationMode.equals("private") || notificationMode.equals("broadcast"))
            kudosManager.playSound(sender, targetPlayer, config.getString("kudo-award.notification.playsound-type"));
    }

    private ArrayList<ItemStack> getItemRewards() {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        String configItemRewardsPath = "kudo-award.rewards.items";
        for (String itemKey : config.getConfigurationSection(configItemRewardsPath).getKeys(false)) {
            String configItemRewardsPathKey = configItemRewardsPath + "." + itemKey;
            Material material = Material.getMaterial(config.getString(configItemRewardsPathKey + ".material"));
            ItemStack itemStack = new ItemStack(material);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemStack.setAmount(config.getInt(configItemRewardsPathKey + ".amount"));
            itemMeta.setDisplayName(config.getString(configItemRewardsPathKey + ".item-name"));
//            if (config.getBoolean("kudo-award.rewards.award-item.use-lore")) itemMeta.setLore(config.getStringList("kudo-award.rewards.award-item.item-lore"));
            itemStack.setItemMeta(itemMeta);
            itemStacks.add(itemStack);
        }
        return itemStacks;
    }
}
