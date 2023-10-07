package Utils.KudosUtils;

import Utils.ItemCreator;
import Utils.SQL.SQLGetter;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class KudosGUI implements Listener {
    private Main plugin = Main.getPlugin(Main.class);
    private SQLGetter data = plugin.data;
    private FileConfiguration guiConfig = plugin.guiConfig;

    public StaticPane createKudosMainPane(Player player) {
        StaticPane kudosMainPane = new StaticPane(0, 0, 9, 1);

        if (guiConfig.getBoolean("slot.statistics.enabled")) {
            ItemCreator statisticsItemItemStack = new ItemCreator(guiConfig.getString("slot.statistics.item"))
                    .setDisplayName(guiConfig.getString("slot.statistics.item-name"))
                    .setLore(setStatisticsValuesLore(guiConfig.getStringList("slot.statistics.lore"), player))
                    .replaceSkullWithPlayerSkull(player);
            GuiItem guiItem = new GuiItem(statisticsItemItemStack.get());
            kudosMainPane.addItem(guiItem, Slot.fromIndex(guiConfig.getInt("slot.statistics.item-slot")));
        }
        if (guiConfig.getBoolean("slot.help.enabled")) {
            ItemCreator helpItemItemStack = new ItemCreator(guiConfig.getString("slot.help.item"))
                    .setDisplayName(guiConfig.getString("slot.help.item-name"))
                    .setLore(guiConfig.getStringList("slot.help.lore"))
                    .replaceSkullWithPlayerSkull(player);
            GuiItem helpItem = new GuiItem(helpItemItemStack.get());
            kudosMainPane.addItem(helpItem, Slot.fromIndex(guiConfig.getInt("slot.help.item-slot")));
        }
        if (guiConfig.getBoolean("slot.kudos-leaderboard.enabled")) {
            ItemCreator leaderboardItemItemStack = new ItemCreator(guiConfig.getString("slot.kudos-leaderboard.item"))
                    .setDisplayName(guiConfig.getString("slot.kudos-leaderboard.item-name"))
                    .setLore(data.getTopPlayersKudos())
                    .replaceSkullWithPlayerSkull(player);
            GuiItem leaderboardItem = new GuiItem(leaderboardItemItemStack.get());
            kudosMainPane.addItem(leaderboardItem, Slot.fromIndex(guiConfig.getInt("slot.kudos-leaderboard.item-slot")));
        }
        return kudosMainPane;
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
}

