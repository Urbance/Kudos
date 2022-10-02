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

import java.util.Arrays;
import java.util.List;

public class GUI implements Listener {
    private final Inventory inventory;

    public GUI() {
        inventory = Bukkit.createInventory(null, 9, "Kudos");
        setItems();
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void setItems() {
        inventory.setItem(2, createItem(Material.PLAYER_HEAD, "§2§lDeine Kudos", null));
        inventory.setItem(4, createItem(Material.POPPY, "§e§lHilfe",
                Arrays.asList("§7/kudo [player] - Award a player a Kudo", "§7/kudos - Opens GUI", "§7/kudos [playername] - Displays the player's Kudos")));
        inventory.setItem(6, createItem(Material.EMERALD, "§b§lTop3", null));
    }

    private ItemStack createItem(Material material, String displayname, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(displayname);
        itemMeta.setLore(lore);

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

        ItemStack playerHead = inventory.getItem(2);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));

        playerHead.setItemMeta(skullMeta);
    }
}

