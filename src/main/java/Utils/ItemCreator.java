package Utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ItemCreator {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private int amount;

    public ItemCreator(String material) {
        this.itemStack = new ItemStack(Material.valueOf(material));
        this.itemMeta = itemStack.getItemMeta();
        this.amount = 1;
    }

    public ItemCreator setDisplayName(String displayName) {
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        return this;
    }

    public ItemCreator setAmount(int amount) {
        this.amount = amount;
        itemStack.setAmount(this.amount);
        return this;
    }

    public ItemCreator setLore(List<String> lore) {
        lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
        itemMeta.setLore(lore);
        return this;
    }

    public ItemCreator replaceSkullWithPlayerSkull(OfflinePlayer offlinePlayer) {
        if (!(itemStack.getType() == Material.PLAYER_HEAD)) return this;

        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
        skullMeta.setOwningPlayer(offlinePlayer);
        skullMeta.setDisplayName(itemMeta.getDisplayName());
        skullMeta.setLore(itemMeta.getLore());
        this.itemMeta = skullMeta;
        return this;
    }

    public ItemStack get() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
