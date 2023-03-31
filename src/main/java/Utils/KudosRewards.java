package Utils;

import de.urbance.Main;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class KudosRewards {
    private Inventory inventory;
    private FileConfiguration config;

    public KudosRewards(Inventory inventory) {
        Main plugin = JavaPlugin.getPlugin(Main.class);
        this.inventory = inventory;
        this.config = plugin.config;
    }

    public boolean addAwardItem() {
        ItemCreator itemCreator = new ItemCreator(Material.getMaterial(config.getString("kudo-award.rewards.award-item.item")));
        ItemStack awardItem = itemCreator.getItemReward();

        if (!itemCanBeAddedToInventory(awardItem, inventory))
            return false;

        inventory.addItem(awardItem);
        return true;
    }

    private boolean itemCanBeAddedToInventory(ItemStack itemStack, Inventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                return true;
            }
            if (inventory.getItem(i).isSimilar(itemStack) && !(inventory.getItem(i).getAmount() + config.getInt("kudo-award.rewards.award-item.amount") > 64)) {
                return true;
            }
        }
        return false;
    }
}
