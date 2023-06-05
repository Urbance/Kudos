package Utils.KudosUtils;

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

public class KudosGUI implements Listener {
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration guiConfig;
    public SQLGetter data = new SQLGetter(plugin);

    public KudosGUI() {
        this.guiConfig = plugin.guiConfig;
    }

    public Inventory create(Player player) {
        String inventoryTitle = guiConfig.getString("title");
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', inventoryTitle));

        ItemStack statisticsItem = createItem(
                player,
                Material.getMaterial(guiConfig.getString("slot.statistics.item")),
                guiConfig.getString("slot.statistics.item-name"),
                guiConfig.getStringList("slot.statistics.lore"));
        ItemStack helpItem = createItem(
                null,
                Material.getMaterial(guiConfig.getString("slot.help.item")),
                guiConfig.getString("slot.help.item-name"),
                guiConfig.getStringList("slot.help.lore"));
        ItemStack topKudosPlayersItem = createItem(
                null,
                Material.getMaterial(guiConfig.getString("slot.top-kudos-players.item")),
                guiConfig.getString("slot.top-kudos-players.item-name"),
                data.getTopPlayersKudos());

        inventory.setItem(guiConfig.getInt("slot.statistics.item-slot"), statisticsItem);
        inventory.setItem(guiConfig.getInt("slot.help.item-slot"), helpItem);
        inventory.setItem(guiConfig.getInt("slot.top-kudos-players.item-slot"), topKudosPlayersItem);

        return inventory;
    }

    private ItemStack createItem(Player player, Material material, String displayName, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));

        itemMeta.setLore(setLore(lore, player));
        item.setItemMeta(itemMeta);
        return item;
    }

    private List<String> setLore(List<String> lore, Player player) {
        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i)));
                if (player != null) {
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i).replaceAll("%kudos_player_kudos%", String.valueOf(data.getKudos(player.getUniqueId())))));
                    lore.set(i, ChatColor.translateAlternateColorCodes('&', lore.get(i).replaceAll("%kudos_player_assigned_kudos%", String.valueOf(data.getAssignedKudo(player.getUniqueId())))));
                }
            }
        }
        return lore;
    }

    private void updatePlayerHead(Inventory inventory, Player player) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) continue;
            ItemStack itemStack = inventory.getItem(slot);
            if (!(itemStack.getType() == Material.PLAYER_HEAD)) continue;

            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
            itemStack.setItemMeta(skullMeta);
        }
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

        if (!event.getInventory().equals(inventory))
            return;

        updatePlayerHead(inventory, player);
    }
}

