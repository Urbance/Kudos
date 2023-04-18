package Utils;

import de.urbance.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ItemCreator {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private FileConfiguration config;
    private int amount;

    public ItemCreator(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        this.config = JavaPlugin.getPlugin(Main.class).config;
        this.amount = 1;
    }

    public void setDisplayName(String displayName) {
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
    }

    public void setLore(List<String> lore) {
        lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
        itemMeta.setLore(lore);
    }

    public void setAmount(int amount) {
        this.amount = amount;
        itemStack.setAmount(this.amount);
    }

    public ItemStack getItemReward() {
        setDisplayName(config.getString("kudo-award.rewards.award-item.item-name"));
        setAmount(config.getInt("kudo-award.rewards.award-item.amount"));
        if (config.getBoolean("kudo-award.rewards.award-item.use-lore")) setLore(config.getStringList("kudo-award.rewards.award-item.item-lore"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getMilestoneItemReward() {
        setDisplayName(config.getString("kudo-award.milestones.rewards.award-item.item-name"));
        setAmount(config.getInt("kudo-award.milestones.rewards.amount"));
        if (config.getBoolean("kudo-award.milestones.rewards.award-item.use-lore")) setLore(config.getStringList("kudo-award.milestones.rewards.item-lore"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
