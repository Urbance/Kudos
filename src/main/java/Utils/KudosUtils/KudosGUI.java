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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KudosGUI implements Listener {
    private Main plugin = Main.getPlugin(Main.class);
    private SQLGetter data = plugin.data;
    private FileConfiguration guiConfig = plugin.guiConfig;
    private PaginatedPane paginatedPane;
    private ChestGui kudosGUI;
    private Player player;
    private int maximumPages;
    private List<String> receivedKudosList;
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
            // TODO refactor to gui.yml
            List<String> lore = new ArrayList<>();
            lore.add("You don't have any received Kudos.");
            if (data.getAmountKudos(player.getUniqueId()) > 0) {
                lore.clear();
                lore = guiConfig.getStringList("slot.receivedKudos.lore");
            }
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
        /*
        ToDo-List
        - translations
        - playsound on page switching
        - kudo by server: gear head
        - arrow heads loading issue
         */

        this.receivedKudosList = data.getAllPlayerKudos(player.getUniqueId(), SQLGetter.FormattingStyle.KUDOS_GUI_RECEIVED_KUDOS);

        // calculate maximum needed pages
        int totalEntries = receivedKudosList.size();

        if (receivedKudosList.isEmpty()) return;
        receivedKudosExists = true;

        int entriesPerPage = 5;
        this.maximumPages = (int) Math.ceil((double) totalEntries / entriesPerPage);

        createReceivedKudosPanes(entriesPerPage, totalEntries);
    }

    private void createReceivedKudosPanes(int entriesPerPage, int totalEntries) {
        for (int pageCounter = 1; pageCounter <=  maximumPages; pageCounter++) {
            StaticPane staticPane = new StaticPane(0, 0, 9, 1);
            paginatedPane.addPane(pageCounter, staticPane);
            fillReceivedKudosPane(staticPane, pageCounter, entriesPerPage, totalEntries);
        }
    }

    private void fillReceivedKudosPane(StaticPane staticPane, int page, int entriesPerPage, int totalEntries) {
        GuiItem arrowLeft = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .setDisplayName("&c&lBack")
                .replaceSkullWithPlayerSkull(Bukkit.getOfflinePlayer("MHF_ArrowLeft"))
                .get(), inventoryClickEvent -> {
            paginatedPane.setPage(page - 1);
            kudosGUI.update();
            Bukkit.broadcastMessage("Go to page " + (page - 1));
        });
        GuiItem arrowRight = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .replaceSkullWithPlayerSkull(Bukkit.getOfflinePlayer("MHF_ArrowRight"))
                .setDisplayName("&c&lForward")
                .get(), inventoryClickEvent -> {
            paginatedPane.setPage(page + 1);
            kudosGUI.update();
            Bukkit.broadcastMessage("Go to page " + (page + 1));
        });

        int inventorySlot = 2;
        int firstKudosReceivedListEntry = (page - 1) * 5;
        int lastKudosReceivedListEntry = Math.min(firstKudosReceivedListEntry + entriesPerPage, totalEntries);

        for (int entry = firstKudosReceivedListEntry; entry < lastKudosReceivedListEntry; entry++) {
            ItemCreator playerHead = new ItemCreator("PLAYER_HEAD")
                    .replaceSkullWithPlayerSkull(player) // replace with player from received kudos list
                    .setDisplayName("&2" + player.getName());
            String[] unsplittedList = receivedKudosList.get(entry).split("\\|");
            List<String> lore = new ArrayList<>(Arrays.asList(unsplittedList));

            playerHead.setLore(lore);
            staticPane.addItem(new GuiItem(playerHead.get()), Slot.fromIndex(inventorySlot));
            inventorySlot++;
        }

        staticPane.addItem(arrowLeft, Slot.fromIndex(0));
        if (page != maximumPages) staticPane.addItem(arrowRight, Slot.fromIndex(8));
    }

//    private void fillReceivedKudosPaneFoo() {
//
//
//
//
//        int startSlot = 2;
//        int endIndex = Math.min(entriesPerPage, totalEntries);
//
//        for (int slot = startSlot; slot < endIndex; slot++) {
//            ItemCreator playerHeadFoo = new ItemCreator("PLAYER_HEAD")
//                    .replaceSkullWithPlayerSkull(player) // replace with player from received kudos list
//                    .setDisplayName("&2" + player.getName());
//
//            String[] unsplittedList = receivedKudosList.get(slot).split("\\|");
//            List<String> lore = new ArrayList<>(Arrays.asList(unsplittedList));
//
//            playerHeadFoo.setLore(lore);
//            staticPane.addItem(new GuiItem(playerHeadFoo.get()), Slot.fromIndex(inventorySlotCounter));
//            inventorySlotCounter++;
//        }
//            staticPane.addItem(arrowLeft, Slot.fromIndex(0));
//            staticPane.addItem(arrowRight, Slot.fromIndex(8));
//
//
//    }

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

