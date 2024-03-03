package Utils.KudosUtils;

/*
    requires InventoryFramework
 */

import Utils.ItemCreator;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class UrbanceGUI {
    private Main plugin;
    private FileConfiguration guiConfig;
    private ChestGui chestGui;

    public UrbanceGUI() {
        this.plugin = Main.getPlugin(Main.class);
        this.guiConfig = plugin.guiConfig;
    }

    public UrbanceGUI create(String title, int size) {;
        this.chestGui = new ChestGui(size, ChatColor.translateAlternateColorCodes('&', title));
        return this;
    }

    public UrbanceGUI cancelOnGlobalClick(boolean cancelOnGlobalClick) {
        chestGui.setOnGlobalClick(event -> event.setCancelled(cancelOnGlobalClick));
        return this;
    }

    public GuiItem getBackwardsPageSwitcher() {
        String arrowLeftURLSkull = "http://textures.minecraft.net/texture/bd69e06e5dadfd84e5f3d1c21063f2553b2fa945ee1d4d7152fdc5425bc12a9";
        GuiItem pageSwitchterLeft = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .setDisplayName(guiConfig.getString("general.page-switcher.backwards.item-name"))
                .replaceSkullWithCustomURLSkull(arrowLeftURLSkull)
                .get());

        return pageSwitchterLeft;
    }

    public GuiItem getForwardsPageSwitcher() {
        String arrowRightURLSkull = "http://textures.minecraft.net/texture/19bf3292e126a105b54eba713aa1b152d541a1d8938829c56364d178ed22bf";
        GuiItem pageSwitchterRight = new GuiItem(new ItemCreator("PLAYER_HEAD")
                .replaceSkullWithCustomURLSkull(arrowRightURLSkull)
                .setDisplayName(guiConfig.getString("general.page-switcher.forwards.item-name"))
                .get());

        return pageSwitchterRight;
    }

    public void playsoundPageSwitcher(Player player) {
        if (!guiConfig.getBoolean("general.page-switcher.playsound.enabled")) return;

        String sound = guiConfig.getString("general.page-switcher.playsound.playsound-type");
        player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);
    }

    public ChestGui get() {
        return chestGui;
    }
}
