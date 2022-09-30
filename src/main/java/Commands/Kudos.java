package Commands;

import Utils.GUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class Kudos implements CommandExecutor {
    public static Inventory inventory;

    @Override

    // TODO Add permission query
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 0) {
                Player player = ((Player) sender).getPlayer();
                inventory = new GUI().getInventory();
                player.openInventory(inventory);
            }
        } else {
            Bukkit.getServer().getLogger().info("You can't execute this command as console!");
        }
        return false;
    }
}
