package Utils.KudosUtils;

import Utils.ItemCreator;
import Utils.SQL.SQLGetter;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class KudosGUI implements Listener {
    private Main plugin = Main.getPlugin(Main.class);
    private SQLGetter data = plugin.data;
    private FileConfiguration guiConfig = plugin.guiConfig;
    private PaginatedPane paginatedPane;
    private ChestGui kudosGUI;
    private Player player;
    private int lastPage;
    private HashMap<Integer, String> receivedKudosList;
    private boolean receivedKudosExists = false;

    private void createGUI() {
        String guiTitle = guiConfig.getString("general.title");
        this.kudosGUI = new ChestGui(1, ChatColor.translateAlternateColorCodes('&', guiTitle));
        kudosGUI.setOnGlobalClick(event -> event.setCancelled(true));
        setPages();
    }

    private void setPages() {
        this.paginatedPane = new PaginatedPane(0, 0, 9, 1);
        paginatedPane.addPane(0, getKudosMainPane(this.player));
        kudosGUI.addPane(paginatedPane);
        setReceivedKudosPages();
    }

    private StaticPane getKudosMainPane(Player player) {
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
        if (guiConfig.getBoolean("slot.receivedKudos.enabled")) {
            List<String> lore = guiConfig.getStringList("slot.receivedKudos.lore-no-received-kudos");
            if (data.getAmountKudos(player.getUniqueId()) > 0) lore = guiConfig.getStringList("slot.receivedKudos.lore");
            ItemCreator receivedKudosItemItemCreator = new ItemCreator(guiConfig.getString("slot.receivedKudos.item"))
                    .setDisplayName(guiConfig.getString("slot.receivedKudos.item-name"))
                    .setLore(lore);

            GuiItem receivedKudosItem = new GuiItem(receivedKudosItemItemCreator.get(), event -> {
                if (!receivedKudosExists) return;
                paginatedPane.setPage(1);
                kudosGUI.update();
            });
            kudosMainPane.addItem(receivedKudosItem, Slot.fromIndex(guiConfig.getInt("slot.receivedKudos.item-slot")));
        }
        return kudosMainPane;
    }

    private void setReceivedKudosPages() {
        this.receivedKudosList = data.getPlayerReceivedKudosGUI(player.getUniqueId());

        // calculate maximum needed pages
        int totalEntries = receivedKudosList.size();

        if (receivedKudosList.isEmpty()) return;
        receivedKudosExists = true;

        int entriesPerPane = 5;
        this.lastPage = (int) Math.ceil((double) totalEntries / entriesPerPane);

        createReceivedKudosPanes(entriesPerPane, totalEntries);
    }

    private void createReceivedKudosPanes(int entriesPerPane, int totalEntries) {
        for (int page = 1; page <=  lastPage; page++) {
            StaticPane staticPane = new StaticPane(0, 0, 9, 1);
            paginatedPane.addPane(page, staticPane);
            fillReceivedKudosPane(staticPane, page, entriesPerPane, totalEntries);
        }
    }

    private void fillReceivedKudosPane(StaticPane staticPane, int currentPage, int entriesPerPane, int totalEntries) {
        GuiItem arrowLeft = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .setDisplayName(guiConfig.getString("slot.receivedKudos.backwards-item-name"))
                .replaceSkullWithPlayerSkull(Bukkit.getOfflinePlayer("MHF_ArrowLeft"))
                .get(), inventoryClickEvent -> {
            paginatedPane.setPage(currentPage - 1);
            kudosGUI.update();
            Bukkit.broadcastMessage("Go to page " + (currentPage - 1));
        });
        GuiItem arrowRight = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .replaceSkullWithPlayerSkull(Bukkit.getOfflinePlayer("MHF_ArrowRight"))
                .setDisplayName(guiConfig.getString("slot.receivedKudos.forwards-item-name"))
                .get(), inventoryClickEvent -> {
            paginatedPane.setPage(currentPage + 1);
            kudosGUI.update();
            Bukkit.broadcastMessage("Go to page " + (currentPage + 1));
        });

        int inventorySlot = 2;
        int firstKudoReceivedListEntry = (currentPage - 1) * 5;
        int lastKudoReceivedListEntry = Math.min(firstKudoReceivedListEntry + entriesPerPane, totalEntries);

        for (int entry = firstKudoReceivedListEntry; entry < lastKudoReceivedListEntry; entry++) {
            String[] unsplittedReceivedKudosList = receivedKudosList.get(entry).split("@");
            String playerName = unsplittedReceivedKudosList[0];
            String awardReason = unsplittedReceivedKudosList[1];
            String awardDate = unsplittedReceivedKudosList[2];

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);

            List<String> itemLore = guiConfig.getStringList("slot.receivedKudos.lore-received-kudos");

            ArrayList<String> modifiedItemLore = new ArrayList<>();
            for (String itemLoreEntry : itemLore) {
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_reason%", awardReason);
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_date%", awardDate);
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_player%", playerName);
                modifiedItemLore.add(itemLoreEntry);
            }

            ItemCreator playerHead = new ItemCreator("PLAYER_HEAD")
                    .replaceSkullWithPlayerSkull(offlinePlayer) // replace with player from received kudos list
                    .setDisplayName("&2" + offlinePlayer.getName())
                    .setLore(modifiedItemLore);
            staticPane.addItem(new GuiItem(playerHead.get()), Slot.fromIndex(inventorySlot));
            inventorySlot++;
        }

        staticPane.addItem(arrowLeft, Slot.fromIndex(0));
        if (currentPage != lastPage) staticPane.addItem(arrowRight, Slot.fromIndex(8));
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

    public void open(Player player) {
        this.player = player;
        createGUI();
        kudosGUI.show(player);
    }
}

