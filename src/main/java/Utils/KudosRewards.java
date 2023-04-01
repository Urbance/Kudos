package Utils;

import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class KudosRewards {
    private Main plugin;
    private Inventory inventory;
    private FileConfiguration config;

    public KudosRewards(Inventory inventory) {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.inventory = inventory;
        this.config = plugin.config;
    }

    public boolean addAwardItem() {
        ItemCreator itemCreator = new ItemCreator(Material.getMaterial(config.getString("kudo-award.rewards.award-item.item")));
        ItemStack awardItem = itemCreator.getItemReward();
        if (!itemCanBeAddedToInventory(awardItem, inventory)) return false;
        inventory.addItem(awardItem);
        return true;
    }

    public void performCommandRewards(Player targetplayer) {
        if (!config.getBoolean("kudo-award.rewards.command-rewards.enabled"))
            return;

        for (String commands : config.getStringList("kudo-award.rewards.command-rewards.commands")) {
            Map<String, String> values = new HashMap<>();
            values.put("kudos_player_name", targetplayer.getName());
            String command = new KudosMessage(plugin).setPlaceholders(commands, values);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    private boolean itemCanBeAddedToInventory(ItemStack itemStack, Inventory inventory) {
        return inventory.firstEmpty() != -1 || inventory.addItem(itemStack).isEmpty();
    }

}
