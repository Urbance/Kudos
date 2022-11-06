package Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemCreator {
    public Material material;
    public String displayName;
    public List<String> lore;
    public int amount;
    public boolean setLore;

    public ItemCreator(Material material, String displayName, List<String> lore, int amount, boolean setLore) {
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.amount = amount;
        this.setLore = setLore;
    }

    public ItemStack create() {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!(displayName == null))
            itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (setLore) {
            lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(amount);

        return itemStack;
    }
}
