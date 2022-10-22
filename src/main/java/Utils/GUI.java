package Utils;

import Commands.Kudos;
import Utils.SQL.SQL;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class GUI implements Listener {
    private final Inventory inventory;
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration locale = plugin.locale;
    public SQLGetter data = new SQLGetter(plugin);

    public GUI() {
        inventory = Bukkit.createInventory(null, 9, "Kudos");
        SQLGetter data = new SQLGetter(plugin);
        setItems();
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void setItems() {
        inventory.setItem(2, createItem(Material.PLAYER_HEAD, locale.getString("GUI.your_kudos.item_name"), null));
        inventory.setItem(4, createItem(Material.POPPY, locale.getString("GUI.help.item_name"), locale.getStringList("GUI.help.lore")));
        inventory.setItem(6, createItem(Material.EMERALD, locale.getString("GUI.top3.item_name"), data.getTemp()));
    }

    private ItemStack createItem(Material material, String displayname, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayname));

        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
            }
        }
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
        SQLGetter data = new SQLGetter(plugin);
        data = new SQLGetter(plugin);

        ItemStack playerHead = inventory.getItem(2);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();

        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));

        List<String> lore = locale.getStringList("GUI.your_kudos.lore");
        lore.set(0, ChatColor.translateAlternateColorCodes('&', lore.get(0)));
        lore.set(0, lore.get(0).replaceAll("%player_kudos%", String.valueOf(data.getKudos(player.getUniqueId()))));

        skullMeta.setLore(lore);
        playerHead.setItemMeta(skullMeta);
    }
}

