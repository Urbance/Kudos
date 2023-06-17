package Utils.KudosUtils;

import Utils.ItemCreator;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
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

public class KudosMilestone {
    private Main plugin;
    private FileConfiguration config;
    private SQLGetter data;
    private FileConfiguration locale;
    private KudosMessage kudosMessage;
    private KudosManager kudosManager;

    public KudosMilestone() {
        this.plugin = JavaPlugin.getPlugin(Main.class);
        this.config = plugin.config;
        this.data = new SQLGetter(plugin);
        this.locale = plugin.localeConfig;
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManager = new KudosManager();
    }

    public boolean sendMilestone(CommandSender sender, Player targetPlayer) {
        if (!sendRewards(sender, targetPlayer)) {
            if (sender instanceof Player) plugin.cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
            return false;
        }

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        placeholderValues.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayer.getUniqueId()) + 1));

        if (sender instanceof ConsoleCommandSender) kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone-through-console"), placeholderValues));
        if (sender instanceof Player) {
            placeholderValues.put("kudos_player_name", sender.getName());
            kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone"), placeholderValues));
        }
        playMilestoneSound(sender, targetPlayer);
        return true;
    }

    private boolean sendRewards(CommandSender sender, Player targetPlayer) {
        if (!addAwardItem(sender, targetPlayer)) return false;
        new KudosManager().performCommandRewards(KudosManager.AwardType.MILESTONE, targetPlayer);
        targetPlayer.giveExp(config.getInt("kudo-award.milestones.rewards.xp"));
        return true;
    }

    private boolean addAwardItem(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("kudo-award.milestones.rewards.award-item.enabled")) {
            return true;
        }

        Inventory inventory = targetPlayer.getInventory();
        ItemStack awardItem = new ItemCreator(Material.getMaterial(config.getString("kudo-award.milestones.rewards.award-item.item"))).getMilestoneItemReward();

        if (!kudosManager.itemCanBeAddedToInventory(awardItem, inventory)) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-inventory-is-full"), placeholderValues));
            return false;
        }

        inventory.addItem(awardItem);
        return true;
    }

    private void playMilestoneSound(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("kudo-award.milestones.enable-playsound"))
            return;
        kudosManager.playSound(sender, targetPlayer, config.getString("kudo-award.milestones.playsound-type"));
    }
}
