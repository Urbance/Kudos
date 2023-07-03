package Utils.KudosUtils;

import Utils.ItemCreator;
import de.urbance.Main;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

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
        if (!addAwardItemAndPerformRewards(targetPlayer)) {
            kudosManager.sendInventoryIsFullMessage(sender, targetPlayer);
            return false;
        }
        return true;
    }

    private boolean addAwardItemAndPerformRewards(Player targetPlayer) {
        Inventory inventory = targetPlayer.getInventory();
        if (!addRewardItems(inventory)) return false;

        new KudosManager().performCommandRewards(KudosManager.AwardType.AWARD, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.rewards.xp"));
        return true;
    }

    private boolean addRewardItems(Inventory inventory) {
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
            if (!config.getBoolean(configItemRewardsPathKey + ".enabled")) continue;
            ItemCreator itemCreator = new ItemCreator(Material.getMaterial(config.getString(configItemRewardsPathKey + ".material")));
            itemCreator.setDisplayName(config.getString(configItemRewardsPathKey + ".item-name"));
            itemCreator.setAmount(config.getInt(configItemRewardsPathKey + ".amount"));
            if (config.getBoolean(configItemRewardsPathKey + ".use-lore")) itemCreator.setLore(config.getStringList(configItemRewardsPathKey + ".item-lore"));
            itemStacks.add(itemCreator.get());
        }
        return itemStacks;
    }
}
