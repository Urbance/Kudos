package GUI;

import Utils.ConfigManagement;
import Utils.ItemCreator;
import Utils.KudosUtils.UrbanceGUI;
import Utils.SQL.SQLGetter;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ReceivedKudosGUI implements GUI_Interface, Listener {
    private Main plugin;
    private SQLGetter data;
    private FileConfiguration receivedKudosConfig;
    private ChestGui receivedKudosGUI;
    private PaginatedPane paginatedPane;
    private HashMap<Integer, String> receivedKudosList;
    private Player player;
    private int lastPage;
    private UrbanceGUI urbanceGUI;

    public ReceivedKudosGUI() {
        this.plugin = Main.getPlugin(Main.class);
        this.data = plugin.data;
        this.receivedKudosConfig = ConfigManagement.getReceivedKudosGuiConfig();
    }

    private void init() {
        createGUI();
        fillGUI();
        this.receivedKudosGUI.addPane(paginatedPane);
    }

    private void fillGUI() {
        this.paginatedPane = new PaginatedPane(0, 0, 9, 1);
        this.receivedKudosList = data.getPlayerReceivedKudosGUI(player.getUniqueId());

        // calculate maximum needed pages
        int totalEntries = receivedKudosList.size();

        if (receivedKudosList.isEmpty()) return;

        int entriesPerPane = 5;
        this.lastPage = (int) Math.ceil((double) totalEntries / entriesPerPane);

        createReceivedKudosPanes(entriesPerPane, totalEntries);
    }

    private void createGUI() {
        this.urbanceGUI = new UrbanceGUI();
        this.receivedKudosGUI = urbanceGUI.create("Kudos", 1)
                .cancelOnGlobalClick(true)
                .get();
    }

    private void createReceivedKudosPanes(int entriesPerPane, int totalEntries) {
        for (int page = 1; page <=  lastPage; page++) {
            StaticPane staticPane = new StaticPane(0, 0, 9, 1);
            paginatedPane.addPane(page, staticPane);
            fillReceivedKudosPane(staticPane, page, entriesPerPane, totalEntries);
        }
    }

    private void fillReceivedKudosPane(StaticPane staticPane, int currentPage, int entriesPerPane, int totalEntries) {
        String serverURLSkull = "http://textures.minecraft.net/texture/b0f10e85418e334f82673eb4940b208ecaee0c95c287685e9eaf24751a315bfa";
        GuiItem pageSwitcherLeft = urbanceGUI.getBackwardsPageSwitcher();

        pageSwitcherLeft.setAction(inventoryClickEvent -> {
            if (currentPage == 1) {
                new OverviewGUI().open(player);
            } else {
                paginatedPane.setPage(currentPage - 1);
                receivedKudosGUI.update();
            }
            urbanceGUI.playsoundPageSwitcher(player);
        });

        GuiItem pageSwitcherRight = urbanceGUI.getForwardsPageSwitcher();
        pageSwitcherRight.setAction(inventoryClickEvent -> {
            paginatedPane.setPage(currentPage + 1);
            receivedKudosGUI.update();
            urbanceGUI.playsoundPageSwitcher(player);
        });

        int inventorySlot = 2;
        int firstKudoReceivedListEntry = (currentPage - 1) * 5;
        int lastKudoReceivedListEntry = Math.min(firstKudoReceivedListEntry + entriesPerPane, totalEntries);

        for (int entry = firstKudoReceivedListEntry; entry < lastKudoReceivedListEntry; entry++) {
            String[] unsplittedReceivedKudosList = receivedKudosList.get(entry).split("@");
            String playerName = unsplittedReceivedKudosList[0].equals(SQLGetter.consoleCommandSenderPrefix) ? ConfigManagement.getConfig().getString("general-settings.console-name") : unsplittedReceivedKudosList[0];
            String awardReason = unsplittedReceivedKudosList[1];
            String awardDateString = unsplittedReceivedKudosList[2];

            LocalDateTime awardDate = LocalDateTime.parse(awardDateString);
            awardDateString = awardDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            List<String> itemLore = receivedKudosConfig.getStringList("items.received-kudos-item.item-lore");
            ArrayList<String> modifiedItemLore = new ArrayList<>();

            for (String itemLoreEntry : itemLore) {
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_reason%", awardReason);
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_date%", awardDateString);
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_player%", playerName);
                modifiedItemLore.add(itemLoreEntry);
            }

            ItemCreator playerHead = new ItemCreator("PLAYER_HEAD");

            if (playerName.equals(ConfigManagement.getConfig().getString("general-settings.console-name"))) {
                playerHead.setDisplayName("&2" + playerName)
                        .replaceSkullWithCustomURLSkull(serverURLSkull);
            } else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                playerHead.setDisplayName("&2" + offlinePlayer.getName())
                        .replaceSkullWithPlayerSkull(offlinePlayer);
            }

            playerHead.setLore(modifiedItemLore);

            staticPane.addItem(new GuiItem(playerHead.get()), Slot.fromIndex(inventorySlot));
            inventorySlot++;
        }

        staticPane.addItem(pageSwitcherLeft, Slot.fromIndex(0));
        if (currentPage != lastPage) staticPane.addItem(pageSwitcherRight, Slot.fromIndex(8));
    }

    @Override
    public void open(Player player) {
        this.player = player;
        
        init();
        
        paginatedPane.setPage(1);
        receivedKudosGUI.show(player);
    }
}

