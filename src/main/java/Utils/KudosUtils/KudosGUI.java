package Utils.KudosUtils;

import Commands.Kudos;
import Utils.ItemCreator;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class KudosGUI implements Listener {
    private Main plugin = Main.getPlugin(Main.class);
    private SQLGetter data = new SQLGetter(plugin);
    private FileConfiguration guiConfig = plugin.guiConfig;

    public Inventory create(Player player) {
        String inventoryTitle = guiConfig.getString("general.title");
        Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.translateAlternateColorCodes('&', inventoryTitle));
        ItemCreator statisticsItem = new ItemCreator(guiConfig.getString("slot.statistics.item"))
                .setDisplayName(guiConfig.getString("slot.statistics.item-name"));
        ItemStack helpItem = new ItemCreator(guiConfig.getString("slot.help.item"))
                .setDisplayName(guiConfig.getString("slot.help.item-name"))
                .setLore(guiConfig.getStringList("slot.help.lore"))
                .get();
        ItemStack leaderboardItem = new ItemCreator(guiConfig.getString("slot.kudos-leaderboard.item"))
                .setDisplayName(guiConfig.getString("slot.kudos-leaderboard.item-name"))
                .setLore(data.getTopPlayersKudos())
                .get();

        if (guiConfig.getBoolean("slot.statistics.enabled")) {
            statisticsItem.setLore(setStatisticsValuesLore(guiConfig.getStringList("slot.statistics.lore"), player));
            inventory.setItem(guiConfig.getInt("slot.statistics.item-slot"), statisticsItem.get());
        }
        if (guiConfig.getBoolean("slot.help.enabled")) {
            inventory.setItem(guiConfig.getInt("slot.help.item-slot"), helpItem);
        }
        if (guiConfig.getBoolean("slot.kudos-leaderboard.enabled")) {
            inventory.setItem(guiConfig.getInt("slot.kudos-leaderboard.item-slot"), leaderboardItem);
        }
        return inventory;
    }

    private List<String> setStatisticsValuesLore(List<String> lore, Player player) {
        if (lore == null) return null;

        List<String> modifiedLore = new ArrayList<>();
        for (String entry : lore) {
            entry = ChatColor.translateAlternateColorCodes('&', entry);
            if (player != null) {
                entry = entry.replace("%kudos_player_kudos%", String.valueOf(data.getAmountKudos(player.getUniqueId())));
                entry = entry.replace("%kudos_player_assigned_kudos%", String.valueOf(data.getAssignedKudos(player.getUniqueId())));
            }
            modifiedLore.add(entry);
        }
        return modifiedLore;
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

