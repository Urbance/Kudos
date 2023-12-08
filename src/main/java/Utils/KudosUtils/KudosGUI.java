package Utils.KudosUtils;

import Utils.ConfigKey;
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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class KudosGUI implements Listener {
    private final Main plugin;
    private final SQLGetter data;
    private final FileConfiguration guiConfig;
    private PaginatedPane paginatedPane;
    private ChestGui kudosGUI;
    private ChestGui receivedKudosGUI;
    private Player player;
    private int lastPage;
    private HashMap<Integer, String> receivedKudosList;
    private boolean receivedKudosExists;
    private final ConfigKey configKey;

    public KudosGUI() {
        this.plugin = Main.getPlugin(Main.class);
        this.data = plugin.data;
        this.guiConfig = plugin.guiConfig;
        this.receivedKudosExists = false;
        this.configKey = plugin.configKey;
    }

    private void init() {
        if (!createMainKudosGUI()) return;
        createReceivedKudosGUI();
    }

    private boolean createMainKudosGUI() {
        String guiTitle = guiConfig.getString("general.title");
        int size = configKey.guiGeneralRows();

        if (size == 0) {
            plugin.getLogger().warning("Error: Please set the value for the key \"rows\" in the gui.yml between 1 and 6.");
            return false;
        }

        this.kudosGUI = new UrbanceGUI().create(guiTitle, size)
                .cancelOnGlobalClick(true)
                .get();
        this.kudosGUI.addPane(getKudosMainPane(this.player));

        return kudosGUI != null;
    }

    private void createReceivedKudosGUI() {
        this.receivedKudosGUI = new UrbanceGUI().create(kudosGUI.getTitle(), 1)
                .cancelOnGlobalClick(true)
                .get();
        fillReceivedKudosGUI();
        this.receivedKudosGUI.addPane(paginatedPane);
    }

    private StaticPane getKudosMainPane(Player player) {
        StaticPane kudosMainPane = new StaticPane(0, 0, 9, kudosGUI.getRows());

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
        if (guiConfig.getBoolean("slot.received-kudos.enabled")) {
            List<String> lore = guiConfig.getStringList("slot.received-kudos.lore-no-received-kudos");
            if (data.getAmountKudos(player.getUniqueId()) > 0) lore = guiConfig.getStringList("slot.received-kudos.lore");
            ItemCreator receivedKudosItemItemCreator = new ItemCreator(guiConfig.getString("slot.received-kudos.item"))
                    .setDisplayName(guiConfig.getString("slot.received-kudos.item-name"))
                    .setLore(lore);

            GuiItem receivedKudosItem = new GuiItem(receivedKudosItemItemCreator.get(), event -> {
                if (!receivedKudosExists) return;
                openReceivedKudosGUI();
            });
            kudosMainPane.addItem(receivedKudosItem, Slot.fromIndex(guiConfig.getInt("slot.received-kudos.item-slot")));
        }
        return kudosMainPane;
    }

    private void fillReceivedKudosGUI() {
        this.paginatedPane = new PaginatedPane(0, 0, 9, 1);
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
        String arrowLeftURLSkull = "http://textures.minecraft.net/texture/bd69e06e5dadfd84e5f3d1c21063f2553b2fa945ee1d4d7152fdc5425bc12a9";
        String arrowRightURLSkull = "http://textures.minecraft.net/texture/19bf3292e126a105b54eba713aa1b152d541a1d8938829c56364d178ed22bf";
        String serverURLSkull = "http://textures.minecraft.net/texture/b0f10e85418e334f82673eb4940b208ecaee0c95c287685e9eaf24751a315bfa";

        GuiItem arrowLeft = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .setDisplayName(guiConfig.getString("received-kudos.backwards-item.item-name"))
                .replaceSkullWithCustomURLSkull(arrowLeftURLSkull)
                .get(), inventoryClickEvent -> {
            if (currentPage == 1) {
                kudosGUI.show(player);
            } else {
                paginatedPane.setPage(currentPage - 1);
                receivedKudosGUI.update();
            }
        });

        GuiItem arrowRight = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .replaceSkullWithCustomURLSkull(arrowRightURLSkull)
                .setDisplayName(guiConfig.getString("received-kudos.forwards-item.item-name"))
                .get(), inventoryClickEvent -> {
            paginatedPane.setPage(currentPage + 1);
            receivedKudosGUI.update();
        });

        int inventorySlot = 2;
        int firstKudoReceivedListEntry = (currentPage - 1) * 5;
        int lastKudoReceivedListEntry = Math.min(firstKudoReceivedListEntry + entriesPerPane, totalEntries);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (int entry = firstKudoReceivedListEntry; entry < lastKudoReceivedListEntry; entry++) {
            String[] unsplittedReceivedKudosList = receivedKudosList.get(entry).split("@");
            String playerName = unsplittedReceivedKudosList[0].equals(SQLGetter.consoleCommandSenderPrefix) ? plugin.config.getString("general.console-name") : unsplittedReceivedKudosList[0];
            String awardReason = unsplittedReceivedKudosList[1];
            String awardDateString = unsplittedReceivedKudosList[2];
            Date date;

            try {
                date = dateFormat.parse(awardDateString);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            awardDateString = dateFormat.format(date);

            List<String> itemLore = guiConfig.getStringList("received-kudos.received-kudos-item.lore");
            ArrayList<String> modifiedItemLore = new ArrayList<>();

            for (String itemLoreEntry : itemLore) {
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_reason%", awardReason);
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_date%", awardDateString);
                itemLoreEntry = itemLoreEntry.replace("%kudos_award_player%", playerName);
                modifiedItemLore.add(itemLoreEntry);
            }

            ItemCreator playerHead = new ItemCreator("PLAYER_HEAD");

            if (playerName.equals(plugin.config.getString("general.console-name"))) {
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

    private void openReceivedKudosGUI() {
        paginatedPane.setPage(1);
        receivedKudosGUI.show(player);
    }

    public void open(Player player) {
        this.player = player;
        init();
        if (this.kudosGUI == null) {
            String errorMessage = configKey.errorSomethingWentWrongPleaseContactServerAdministrator();
            if (player != null) new KudosMessage(plugin).send(player, errorMessage);
            return;
        }
        kudosGUI.show(player);
    }
}

