package Utils;

import Commands.Kudos;
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
    public FileConfiguration locale;
    public SQLGetter data = new SQLGetter(plugin);

    public GUI() {
        inventory = Bukkit.createInventory(null, 9, "Kudos");
        this.locale = plugin.localeConfig;
        setItems();
    }

    public Inventory getInventory() {
        return inventory;
    }

    private void setItems() {
        inventory.setItem(2, createItem(Material.PLAYER_HEAD, locale.getString("GUI.you.item-name"), null));
        inventory.setItem(4, createItem(Material.POPPY, locale.getString("GUI.help.item-name"), locale.getStringList("GUI.help.lore")));
        inventory.setItem(6, createItem(Material.EMERALD, locale.getString("GUI.top3.item-name"), data.getTopThreePlayers()));
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
        data = new SQLGetter(plugin);

        if (!event.getInventory().equals(inventory))
            return;

        ItemStack playerHead = inventory.getItem(2);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        FileConfiguration locale = new FileManager("messages.yml", plugin).getConfig();
        List<String> lore = locale.getStringList("GUI.you.lore");

        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));

        lore.set(0, ChatColor.translateAlternateColorCodes('&', lore.get(0).replaceAll("%player_kudos%", String.valueOf(data.getKudos(player.getUniqueId())))));
        lore.set(1, ChatColor.translateAlternateColorCodes('&', lore.get(1).replaceAll("%player_assigned_kudos%", String.valueOf(data.getAssignedKudo(player.getUniqueId())))));

        skullMeta.setLore(lore);
        playerHead.setItemMeta(skullMeta);
    }
}

