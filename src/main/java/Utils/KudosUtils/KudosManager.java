package Utils.KudosUtils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class KudosManager {
    private Main plugin;
    private SQLGetter data;
    private FileConfiguration config;
    private FileConfiguration locale;
    private KudosMessage kudosMessage;

    public KudosManager() {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.data = new SQLGetter(plugin);
        this.config = plugin.config;
        this.locale = plugin.localeConfig;
        this.kudosMessage = new KudosMessage(plugin);
    }

    public enum AwardType {
        AWARD,
        MILESTONE
    }

    public void addKudo(CommandSender sender, UUID targetPlayerUUID) {
        if (sender instanceof Player) {
            data.addKudos(targetPlayerUUID, ((Player) sender).getUniqueId(), 1);
        } else {
            data.addKudos(targetPlayerUUID, null, 1);
        }
    }

    public void showPlayerKudos(CommandSender sender, OfflinePlayer targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId())));
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("kudos.show-player-kudos"), values));
    }

    public void playSound(CommandSender sender, Player targetPlayer, String playSoundType) {
        String prefix = plugin.prefix;

        if (getNotificationMode().equals("private") && !isMilestone(targetPlayer) && sender instanceof Player) {
            Player player = (Player) sender;
            player.playSound(player.getLocation(), Sound.valueOf(playSoundType), 1, 1);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.valueOf(playSoundType), 1, 1);
            return;
        }

        for (Player players : Bukkit.getOnlinePlayers()) {
            try {
                players.playSound(players, Sound.valueOf(playSoundType), 1, 1);
            }
            catch (Exception e) {
                // TODO Check if var playSoundType is printed correctly
                Bukkit.getLogger().warning(prefix + "Error in the config: playsound-type \"" + playSoundType + "\" isn't a valid playsound!");
                return;
            }
        }
    }

    public String getNotificationMode() {
        if (config.getString("kudo-award.notification.notification-mode").equals("private")) {
            return "private";
        }
        return "broadcast";
    }

    public boolean isMilestone(Player targetPlayer) {
        if (config.getBoolean("kudo-award.milestones.enabled")) {
            int targetPlayerKudos = data.getKudos(targetPlayer.getUniqueId()) + 1;
            return targetPlayerKudos % config.getInt("kudo-award.milestones.span-between-kudos") == 0;
        }
        return false;
    }

    public boolean itemCanBeAddedToInventory(ArrayList<ItemStack> itemsThatShouldBeAdded, Inventory inventory) {
        ArrayList<ItemStack> itemsThatCanBeAdded = new ArrayList<>();
        for (ItemStack itemStack : itemsThatShouldBeAdded) {
            if (inventory.addItem(itemStack).isEmpty()) {
                itemsThatCanBeAdded.add(itemStack);
            }
        }
        if (itemsThatCanBeAdded.size() == itemsThatShouldBeAdded.size()) return true;
        for (ItemStack addedItems : itemsThatCanBeAdded) {
            inventory.removeItem(addedItems);
        }
        return false;
    }

    public void sendInventoryIsFullMessage(CommandSender sender, Player targetPlayer) {
        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-inventory-is-full"), placeholderValues));
    }

    public void performCommandRewards(AwardType awardType, Player targetPlayer) {
        switch (awardType) {
            case AWARD -> {
                if (!config.getBoolean("kudo-award.rewards.command-rewards.enabled"))
                    return;
                for (String commands : config.getStringList("kudo-award.rewards.command-rewards.commands")) {
                    Map<String, String> values = new HashMap<>();
                    values.put("kudos_player_name", targetPlayer.getName());
                    String command = new KudosMessage(plugin).setPlaceholders(commands, values);
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
            case MILESTONE -> {
                if (!config.getBoolean("kudo-award.milestones.rewards.command-rewards.enabled"))
                    return;
                for (String commands : config.getStringList("kudo-award.milestones.rewards.command-rewards.commands")) {
                    Map<String, String> values = new HashMap<>();
                    values.put("kudos_player_name", targetPlayer.getName());
                    String command = new KudosMessage(plugin).setPlaceholders(commands, values);
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
    }

}
