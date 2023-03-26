package Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemCreator {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private FileConfiguration config;
    private int amount;

    public ItemCreator(Material material, FileConfiguration config) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
        this.config = config;
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
        setDisplayName(config.getString("award-item.item-name"));
        setAmount(config.getInt("award-item.amount"));
        if (config.getBoolean("award-item.use-lore")) setLore(config.getStringList("award-item.item-lore"));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack get() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
