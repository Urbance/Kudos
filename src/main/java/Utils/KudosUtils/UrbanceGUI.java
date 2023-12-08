package Utils.KudosUtils;

/*
    requires InventoryFramework
 */

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import org.bukkit.ChatColor;

public class UrbanceGUI {
    private ChestGui chestGui;

    public UrbanceGUI create(String title, int size) {;
        this.chestGui = new ChestGui(size, ChatColor.translateAlternateColorCodes('&', title));
        return this;
    }

    public UrbanceGUI cancelOnGlobalClick(boolean cancelOnGlobalClick) {
        chestGui.setOnGlobalClick(event -> event.setCancelled(cancelOnGlobalClick));
        return this;
    }

    public ChestGui get() {
        return chestGui;
    }
}
