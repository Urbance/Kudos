package Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUI {
    private Inventory inventory;

    public GUI() {
        inventory = Bukkit.createInventory(null, 27, "Kudos");
        setItems();
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void setItems() {
        inventory.setItem(11, createItem(Material.PLAYER_HEAD, "§2§lDeine Kudos"));
        inventory.setItem(13, createItem(Material.POPPY, "§eVergebe einen Kudo"));
        inventory.setItem(15, createItem(Material.EMERALD, "§b§lTop3"));
        inventory.setItem(22, createItem(Material.PLAYER_HEAD, "§eSpielersuche"));
    }

    private ItemStack createItem(Material material, String displayname) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayname);

        item.setItemMeta(meta);

        return item;
    }

}
