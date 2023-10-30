package Utils.KudosUtils;

import Utils.ItemCreator;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class KudosManagement {
    private Main plugin;
    private SQLGetter data;
    private FileConfiguration config;
    private FileConfiguration locale;
    private KudosMessage kudosMessage;

    public KudosManagement() {
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

    public void addKudo(CommandSender sender, UUID targetPlayerUUID, String reason) {
        if (sender instanceof Player) {
            if (!data.addKudos(targetPlayerUUID, String.valueOf(((Player) sender).getUniqueId()), reason, 1)) kudosMessage.sendSender(sender, "An error has occurred: Please contact the system administrator or the developer of the plugin.");
            return;
        }
        if (!data.addKudos(targetPlayerUUID, SQLGetter.consoleCommandSenderPrefix, reason, 1)) {
            kudosMessage.sendSender(sender, "An error has occurred: Please contact the system administrator or the developer of the plugin.");
        }
    }

    public void showPlayerKudos(CommandSender sender, OfflinePlayer targetPlayer) {
        Map<String, String> values = new HashMap<>();
        values.put("kudos_targetplayer_name", targetPlayer.getName());
        values.put("kudos_targetplayer_kudos", String.valueOf(data.getAmountKudos(targetPlayer.getUniqueId())));
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
            int targetPlayerKudos = data.getAmountKudos(targetPlayer.getUniqueId()) + 1;
            return targetPlayerKudos % config.getInt("kudo-award.milestones.span-between-kudos") == 0;
        }
        return false;
    }

    public boolean itemCanBeAddedToInventory(ArrayList<ItemStack> itemsThatShouldBeAdded, Player targetplayer) {
        ArrayList<ItemStack> itemsThatCanBeAdded = new ArrayList<>();
        Inventory targetplayerInventory = targetplayer.getInventory();
        Inventory dummyInventory = Bukkit.createInventory(null, 36);
        targetplayer.updateInventory();

        // fill dummy inventory
        for (int slot = 0; slot < 36; slot++) {
            ItemStack itemStack = targetplayerInventory.getItem(slot);
            if (itemStack != null) dummyInventory.setItem(slot, targetplayerInventory.getItem(slot));
        }

        // fill list with item rewards that can be added
        for (ItemStack itemStack : itemsThatShouldBeAdded) {
            if (dummyInventory.addItem(itemStack).isEmpty()) {
                itemsThatCanBeAdded.add(itemStack);
            }
        }

        return itemsThatCanBeAdded.size() == itemsThatShouldBeAdded.size();
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

    public boolean addItemRewards(CommandSender sender, Player targetplayer, String configItemRewardsListPath) {
        Inventory targetplayerInventory = targetplayer.getInventory();
        ArrayList<ItemStack> itemStackList = getItemRewards(configItemRewardsListPath);

        if (!itemCanBeAddedToInventory(itemStackList, targetplayer)) {
            sendInventoryIsFullMessage(sender, targetplayer);
            return false;
        }
        for (ItemStack itemStack : itemStackList) {
            targetplayerInventory.addItem(itemStack);
        }
        targetplayer.updateInventory();
        return true;
    }

    public ArrayList<ItemStack> getItemRewards(String configItemRewardsListPath) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (String itemKey : config.getConfigurationSection(configItemRewardsListPath).getKeys(false)) {
            String configItemRewardsPathKey = configItemRewardsListPath + "." + itemKey;
            if (!config.getBoolean(configItemRewardsPathKey + ".enabled")) continue;
            ItemCreator itemCreator = new ItemCreator(config.getString(configItemRewardsPathKey + ".material"));
            itemCreator.setDisplayName(config.getString(configItemRewardsPathKey + ".item-name"));
            itemCreator.setAmount(config.getInt(configItemRewardsPathKey + ".amount"));
            if (config.getBoolean(configItemRewardsPathKey + ".use-lore")) itemCreator.setLore(config.getStringList(configItemRewardsPathKey + ".item-lore"));
            itemStacks.add(itemCreator.get());
        }
        return itemStacks;
    }

    public String getReason(String[] args, int startIndex) {
        String reason = args[startIndex - 1];
        int endIndex = args.length;

        for (int argumentPosition = startIndex; argumentPosition < endIndex; argumentPosition++) reason += " " + args[argumentPosition];

        return reason;
    }
}

