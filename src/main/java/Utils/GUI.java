package Utils;

import Commands.Kudos;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class GUI implements Listener {
    private final Inventory inventory;

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
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(displayname);

        item.setItemMeta(itemMeta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = Kudos.inventory;
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory inventory = Kudos.inventory;

        ItemStack playerHead = inventory.getItem(11);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));

        playerHead.setItemMeta(skullMeta);
    }
}

