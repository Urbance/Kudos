package GUI;

import Utils.ConfigManagement;
import Utils.ItemCreator;
import Utils.KudosUtils.KudosMessage;
import Utils.KudosUtils.UrbanceGUI;
import Utils.SQL.SQLGetter;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LeaderboardGUI implements GUI_Interface {
    private Main plugin;
    private StaticPane staticPane;
    private Player player;
    private ChestGui gui;
    private HashMap<UUID, String> leaderboardData;

    public LeaderboardGUI(HashMap<UUID, String> leaderboardData) {
        this.plugin = Main.getPlugin(Main.class);
        this.leaderboardData = leaderboardData;
    }

    private void init() {
        createGUI();
        fillGUI();
    }

    private void fillGUI() {
        this.staticPane = new StaticPane(0, 0, 9, 1);

        setBackwardsPageSwitcher();
        setLeaderboardPlayers();

        gui.addPane(staticPane);
    }

    private void createGUI() {
        FileConfiguration globalGuiSettingsConfig = ConfigManagement.getGlobalGuiSettingsConfig();
        String guiTitle = globalGuiSettingsConfig.getString("general-settings.gui-title");

        this.gui = new UrbanceGUI().create(guiTitle, 1)
                .cancelOnGlobalClick(true)
                .get();
    }

    private void setBackwardsPageSwitcher() {
        UrbanceGUI urbanceGUI = new UrbanceGUI();
        GuiItem backwardsPageSwitcher = urbanceGUI.getBackwardsPageSwitcher();

        backwardsPageSwitcher.setAction(inventoryClickEvent -> {
            new OverviewGUI().open(player);
            urbanceGUI.playsoundPageSwitcher(player);
        });
        staticPane.addItem(backwardsPageSwitcher, Slot.fromIndex(0));
    }

    private void setLeaderboardPlayers() {
        FileConfiguration leaderboardConfig = ConfigManagement.getLeaderboardGuiConfig();
        SQLGetter data = new SQLGetter(plugin);

        int playerHeadSlot = 2;

        for (UUID entry : leaderboardData.keySet()) {
            String playerName = data.getPlayerDisplayName(entry.toString());

            String playerTotalKudos = leaderboardData.get(entry);
            String itemDisplayName = leaderboardConfig.getString("items.player-leaderboard-item.item-name").replace("%kudos_leaderboard_name%", playerName);

            List<String> itemLore = leaderboardConfig.getStringList("items.player-leaderboard-item.item-lore");
            ArrayList<String> modifiedItemLore = new ArrayList<>();

            for (String itemLoreEntry : itemLore) {
                itemLoreEntry = itemLoreEntry.replace("%kudos_leaderboard_kudos%", playerTotalKudos);
                modifiedItemLore.add(itemLoreEntry);
            }

            ItemCreator itemCreator = new ItemCreator("PLAYER_HEAD");
            GuiItem playerHead = new GuiItem(itemCreator.setDisplayName(itemDisplayName)
                    .setLore(modifiedItemLore)
                    .replaceSkullWithPlayerSkull(Bukkit.getOfflinePlayer(playerName))
                    .get()
            );

            staticPane.addItem(playerHead, Slot.fromIndex(playerHeadSlot));
            playerHeadSlot++;
        }
    }

    @Override
    public void open(Player player) {
        this.player = player;

        init();

        if (this.gui == null) {
            String errorMessage = ConfigManagement.getLocalesConfig().getString("error.something-went-wrong-please-contact-server-administrator");
            if (player != null) new KudosMessage(plugin).send(player, errorMessage);
            return;
        }

        gui.show(player);
    }
}
