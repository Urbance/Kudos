package GUI;

import Utils.ConfigKey;
import Utils.ItemCreator;
import Utils.KudosUtils.KudosMessage;
import Utils.KudosUtils.UrbanceGUI;
import Utils.SQL.SQLGetter;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import de.urbance.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

public class OverviewGUI implements Listener {
    private final Main plugin;
    private final SQLGetter data;
    private final FileConfiguration overviewConfig;
    private ChestGui kudosGUI;
    private Player player;
    private final ConfigKey configKey;
    private StaticPane overviewPane;

    public OverviewGUI() {
        this.plugin = Main.getPlugin(Main.class);
        this.data = plugin.data;
        this.overviewConfig = plugin.overviewConfig;
        this.configKey = plugin.configKey;
    }

    private void createOverviewGui() {
        FileConfiguration globalGuiSettingsConfig = plugin.globalGuiSettingsConfig;
        String guiTitle = globalGuiSettingsConfig.getString("general-settings.gui-title");

        int size = configKey.guiGeneralRows();

        if (size == 0) {
            plugin.getLogger().warning("Error: Please set the value for the key \"rows\" in the overview.yml between 1 and 6.");
            return;
        }

        this.kudosGUI = new UrbanceGUI().create(guiTitle, size)
                .cancelOnGlobalClick(true)
                .get();

        createAndSetPane();
    }

    private void createAndSetPane() {
        this.overviewPane = new StaticPane(0, 0, 9, kudosGUI.getRows());

        addStatisticsGuiItem();
        addHelpGuiItem();
        addLeaderboardGuiItem();
        addReceivedKudosGuiItem();

        kudosGUI.addPane(overviewPane);
    }

    private void addStatisticsGuiItem() {
        if (overviewConfig.getBoolean("items.statistics.enabled")) {
            ItemCreator statisticsItemItemStack = new ItemCreator(overviewConfig.getString("items.statistics.item"))
                    .setDisplayName(overviewConfig.getString("items.statistics.item-name"))
                    .setLore(setStatisticsValuesLore(overviewConfig.getStringList("items.statistics.item-lore"), player))
                    .replaceSkullWithPlayerSkull(player);
            GuiItem guiItem = new GuiItem(statisticsItemItemStack.get());
            overviewPane.addItem(guiItem, Slot.fromIndex(overviewConfig.getInt("items.statistics.item-slot")));
        }
    }

    private void addHelpGuiItem() {
        if (overviewConfig.getBoolean("items.help.enabled")) {
            ItemCreator helpItemItemStack = new ItemCreator(overviewConfig.getString("items.help.item"))
                    .setDisplayName(overviewConfig.getString("items.help.item-name"))
                    .setLore(overviewConfig.getStringList("items.help.item-lore"))
                    .replaceSkullWithPlayerSkull(player);
            GuiItem helpItem = new GuiItem(helpItemItemStack.get());
            overviewPane.addItem(helpItem, Slot.fromIndex(overviewConfig.getInt("items.help.item-slot")));
        }
    }

    private void addLeaderboardGuiItem() {
        if (overviewConfig.getBoolean("items.kudos-leaderboard.enabled")) {
            List<String> leaderboardTopKudosData = data.getTopPlayersKudos(6);
            List<String> lore = configKey.slot_kudos_leaderboard_lore();
            if (leaderboardTopKudosData.isEmpty()) lore = configKey.slot_kudos_leaderboard_lore_no_kudos_exists();

            ItemCreator leaderboardItemItemStack = new ItemCreator(overviewConfig.getString("items.kudos-leaderboard.item"))
                    .setDisplayName(overviewConfig.getString("items.kudos-leaderboard.item-name"))
                    .setLore(lore)
                    .replaceSkullWithPlayerSkull(player);
            GuiItem leaderboardItem = new GuiItem(leaderboardItemItemStack.get(), event -> {
                if (leaderboardTopKudosData.isEmpty()) return;
                new LeaderboardGUI(leaderboardTopKudosData).open(player);
            });
            overviewPane.addItem(leaderboardItem, Slot.fromIndex(overviewConfig.getInt("items.kudos-leaderboard.item-slot")));
        }
    }

    private void addReceivedKudosGuiItem() {
        if (overviewConfig.getBoolean("items.received-kudos.enabled")) {
            List<String> lore = overviewConfig.getStringList("items.received-kudos.item-lore-no-received-kudos");
            if (data.getAmountKudos(player.getUniqueId()) > 0) lore = overviewConfig.getStringList("items.received-kudos.item-lore");
            ItemCreator receivedKudosItemItemCreator = new ItemCreator(overviewConfig.getString("items.received-kudos.item"))
                    .setDisplayName(overviewConfig.getString("items.received-kudos.item-name"))
                    .setLore(lore);

            GuiItem receivedKudosItem = new GuiItem(receivedKudosItemItemCreator.get(), event -> {
                HashMap<Integer, String> receivedKudosList = data.getPlayerReceivedKudosGUI(player.getUniqueId());
                if (receivedKudosList.isEmpty()) return;
                openReceivedKudosGUI();
            });
            overviewPane.addItem(receivedKudosItem, Slot.fromIndex(overviewConfig.getInt("items.received-kudos.item-slot")));
        }
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
        new ReceivedKudosGUI().open(player);
    }

    public void open(Player player) {
        this.player = player;

        createOverviewGui();

        if (this.kudosGUI == null) {
            String errorMessage = configKey.errorSomethingWentWrongPleaseContactServerAdministrator();
            if (player != null) new KudosMessage(plugin).send(player, errorMessage);
            return;
        }

        kudosGUI.show(player);
    }
}

