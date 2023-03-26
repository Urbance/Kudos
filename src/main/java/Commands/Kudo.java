package Commands;

import Utils.*;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Kudo implements CommandExecutor, TabCompleter {
    private Main plugin;
    private String prefix;
    private SQLGetter data;
    private KudosNotification kudosNotification;
    private KudosMessage kudosMessage;
    private KudosManager kudosManager;
    private FileConfiguration locale;
    private FileConfiguration config;
    private int playerCooldown;
    private final CooldownManager cooldownManager = new CooldownManager();
    private final LimitationManager limitationManager = new LimitationManager();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.plugin = Main.getPlugin(Main.class);
        this.locale = plugin.localeConfig;
        this.config = plugin.config;
        this.prefix = plugin.prefix;
        this.data = new SQLGetter(plugin);
        this.kudosNotification = new KudosNotification(plugin);
        this.kudosMessage = new KudosMessage(plugin);
        this.kudosManager = new KudosManager(plugin);

        if (!validateInput(args, sender))
            return false;

        awardKudo(sender, args);

        return false;
    }

    private void awardKudo(CommandSender sender, String[] args) {
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID targetPlayerUUID = targetPlayer.getUniqueId();
        String notificationMode = getNotificationMode();

        if (sender instanceof Player) this.playerCooldown = cooldownManager.getCooldown(((Player) sender).getUniqueId());
        if (!preValidation(sender)) return;
        if (!addKudoAndAwardItem(sender, targetPlayer, targetPlayerUUID)) return;

        sendKudoAwardNotification(sender, targetPlayer, notificationMode);
        setCooldown(sender);
    }

    private boolean addLimitation(CommandSender sender, Player targetPlayer) {
        return limitationManager.setLimitation(sender, targetPlayer);
    }

    private boolean addKudoAndAwardItem(CommandSender sender, Player targetPlayer, UUID targetPlayerUUID) {
        if (config.getBoolean("kudo-award-limitation.enabled")) {
            if (!addLimitation(sender, targetPlayer)) {
                return false;
            }
        }

        if (!kudosManager.addItemReward(sender, targetPlayer)) {
            if (sender instanceof Player) cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
            return false;
        }

        kudosManager.addKudo(sender, targetPlayerUUID);
        return true;
    }

    private void sendKudoAwardNotification(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (sendMilestone(sender, targetPlayer, targetPlayer.getUniqueId()))
            return;
        if (!isKudoAwardNotificationEnabled())
            return;

        playNotificationSound(sender, targetPlayer, notificationMode);

        if (!(sender instanceof Player)) {
            kudosNotification.fromConsole(targetPlayer);
            return;
        }
        if (notificationMode.equals("broadcast")) {
            kudosNotification.sendBroadcastMessage(sender, targetPlayer);
            return;
        }
        if (notificationMode.equals("private")) {
            kudosNotification.sendPrivate(sender, targetPlayer);
        }
    }

    private boolean preValidation(CommandSender sender) {
        return validatePlayerCooldown(sender);
    }

    private boolean sendMilestone(CommandSender sender, Player targetPlayer, UUID targetPlayerUUID) {
        if (!isMilestone(targetPlayer)) {
            return false;
        }
        if (!awardMilestoneReward(sender, targetPlayer)) {
            return true;
        }

        playMilestoneSound(sender, targetPlayer);

        Map<String, String> placeholderValues = new HashMap<>();
        placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());
        placeholderValues.put("kudos_targetplayer_kudos", String.valueOf(data.getKudos(targetPlayerUUID)));

        if (sender instanceof Player) {
            placeholderValues.put("kudos_player_name", sender.getName());
            kudosMessage.broadcast(kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone"), placeholderValues));
            return true;
        }

        if (sender instanceof ConsoleCommandSender) {
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("milestone.player-reaches-milestone-through-console"), placeholderValues));
            return true;
        }
        return false;
    }

    private boolean awardMilestoneReward(CommandSender sender, Player targetPlayer) {
        if (!config.getBoolean("milestone.reward.enabled")) {
            return false;
        }

        Inventory inventory = targetPlayer.getInventory();
        ItemCreator itemCreator = new ItemCreator(Material.getMaterial(config.getString("milestone.reward.item")), config);

        itemCreator.setDisplayName(config.getString("milestone.reward.item-name"));
        itemCreator.setAmount(config.getInt("milestone.reward.amount"));

        if (config.getBoolean("milestone.reward.use-lore"))
            itemCreator.setLore(config.getStringList("milestone.reward.item-lore"));

        ItemStack awardItem = itemCreator.get();

        if (!itemCanBeAddedToInventory(awardItem, inventory)) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_targetplayer_name", targetPlayer.getName());

            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-inventory-is-full"), placeholderValues));

            if (sender instanceof Player)
                cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);
            return false;
        }

        inventory.addItem(awardItem);
        return true;
    }

    private void playMilestoneSound(CommandSender sender, Player targetPlayer) {
        if (!isMilestonePlaysoundEnabled())
            return;
        playSound(sender, targetPlayer, config.getString("milestone.playsound-type"));
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*"))) {
            kudosMessage.noPermission(sender);
            return false;
        }
        if (args.length == 0) {
            kudosMessage.sendSender(sender, locale.getString("error.specify-player"));
            return false;
        }
        if (args.length > 1) {
            kudosMessage.wrongUsage(sender);
            return false;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_targetplayer_name", args[0]);
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.player-not-online"), placeholderValues));
            return false;
        }
        if (sender == Bukkit.getPlayer(args[0])) {
            kudosMessage.sendSender(sender, locale.getString("error.cant-give-yourself-kudo"));
            return false;
        }
        return true;
    }

    private void setCooldown(CommandSender sender) {
        if (!(sender instanceof Player))
            return;
        if (config.getInt("general.kudo-award-cooldown") == 0)
            return;

        UUID senderUUID = ((Player) sender).getUniqueId();
        cooldownManager.setCooldown(senderUUID, config.getInt("general.kudo-award-cooldown"));
        new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = cooldownManager.getCooldown(senderUUID);
                cooldownManager.setCooldown(senderUUID, --timeLeft);
                if (timeLeft == 0)
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private String getNotificationMode() {
        if (config.getString("kudo-award-notification.notification-mode").equals("private")) {
            return "private";
        }
        return "broadcast";
    }

    private boolean isMilestone(Player targetPlayer) {
        if (isMilestoneEnabled()) {
            return validateMilestone(targetPlayer);
        }
        return false;
    }

    private boolean validatePlayerCooldown(CommandSender sender) {
        if (!canAwardKudos() && sender instanceof Player) {
            Map<String, String> placeholderValues = new HashMap<>();
            placeholderValues.put("kudos_cooldown", String.valueOf(playerCooldown));
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(locale.getString("error.must-wait-before-use-again"), placeholderValues));
            return false;
        }
        return true;
    }

    private void playNotificationSound(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (!isKudoAwardPlaysoundEnabled())
            return;

        if (notificationMode.equals("private") || notificationMode.equals("broadcast"))
            playSound(sender, targetPlayer, config.getString("kudo-award-notification.playsound-type"));
    }

    private boolean validateMilestone(Player targetPlayer) {
        int targetPlayerKudos = data.getKudos(targetPlayer.getUniqueId());
        return targetPlayerKudos % config.getInt("milestone.span-between-kudos") == 0;
    }

    private void playSound(CommandSender sender, Player targetPlayer, String playSoundType) {
        if (getNotificationMode().equals("private") && !validateMilestone(targetPlayer) && sender instanceof Player) {
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

    private boolean isMilestoneEnabled() { return config.getBoolean("milestone.enabled");
    }

    private boolean isMilestonePlaysoundEnabled() { return config.getBoolean("milestone.enable-playsound"); }

    private boolean isKudoAwardPlaysoundEnabled() { return config.getBoolean("kudo-award-notification.enable-playsound"); }

    private boolean canAwardKudos() { return playerCooldown == 0; }

    private boolean isKudoAwardNotificationEnabled() {
        return config.getBoolean("kudo-award-notification.enabled");
    }

    private boolean itemCanBeAddedToInventory(ItemStack itemStack, Inventory inventory) {
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                return true;
            }
            if (inventory.getItem(i).isSimilar(itemStack) && !(inventory.getItem(i).getAmount() + config.getInt("award-item.amount") > 64)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<String> list = new ArrayList<>();
        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*")))
            return list;
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                list.add(player.getName());
            }
        }
        return list;
    }
}
