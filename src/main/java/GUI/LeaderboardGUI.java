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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LeaderboardGUI implements GUI_Interface {
    private Main plugin;
    private ConfigKey configKey;
    private StaticPane staticPane;
    private Player player;
    private ChestGui gui;
    private HashMap<UUID, String> leaderboardData;

    public LeaderboardGUI(HashMap<UUID, String> leaderboardData) {
        this.plugin = Main.getPlugin(Main.class);
        this.configKey = plugin.configKey;
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
        // TODO build gui title dynamically -> extra config key?
        this.gui = new UrbanceGUI().create("Kudos", 1)
                .cancelOnGlobalClick(true)
                .get();
    }

    private void setBackwardsPageSwitcher() {
        UrbanceGUI urbanceGUI = new UrbanceGUI();
        GuiItem backwardsPageSwitcher = urbanceGUI.getBackwardsPageSwitcher();

        backwardsPageSwitcher.setAction(inventoryClickEvent -> {
            new KudosGUI().open(player);
            urbanceGUI.playsoundPageSwitcher(player);
        });
        staticPane.addItem(backwardsPageSwitcher, Slot.fromIndex(0));
    }

    private void setLeaderboardPlayers() {
        int playerHeadSlot = 2;

        for (UUID entry : leaderboardData.keySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry);
            String playerTotalKudos = leaderboardData.get(entry);
            String itemDisplayName = configKey.leaderboard_player_leaderboard_item_item_name().replace("%kudos_leaderboard_name%", player.getName());

            List<String> itemLore = configKey.leaderboard_player_leaderboard_item_item_lore();
            ArrayList<String> modifiedItemLore = new ArrayList<>();

            for (String itemLoreEntry : itemLore) {
                itemLoreEntry = itemLoreEntry.replace("%kudos_leaderboard_kudos%", playerTotalKudos);
                modifiedItemLore.add(itemLoreEntry);
            }

            ItemCreator itemCreator = new ItemCreator("PLAYER_HEAD");
            GuiItem playerHead = new GuiItem(itemCreator.setDisplayName(itemDisplayName)
                    .setLore(modifiedItemLore)
                    .replaceSkullWithPlayerSkull(player)
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
            String errorMessage = configKey.errorSomethingWentWrongPleaseContactServerAdministrator();
            if (player != null) new KudosMessage(plugin).send(player, errorMessage);
            return;
        }

        gui.show(player);
    }
}
