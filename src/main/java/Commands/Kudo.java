package Commands;

import Utils.CooldownManager;
import Utils.ItemCreator;
import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Kudo implements CommandExecutor, TabCompleter {
    private final CooldownManager cooldownManager = new CooldownManager();
    public String prefix;
    public SQLGetter data;
    public Main plugin = Main.getPlugin(Main.class);
    public FileConfiguration locale;
    public FileConfiguration config;
    public int timeLeft;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.locale = plugin.localeConfig;
        this.config = plugin.getConfig();
        this.prefix = config.getString("prefix");

        if (!validateInput(args, sender))
            return false;

        awardKudo(sender, args);

        return false;
    }

    private void awardKudo(CommandSender sender, String[] args) {
        data = new SQLGetter(plugin);
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        if (sender instanceof Player)
            this.timeLeft = cooldownManager.getCooldown(((Player) sender).getUniqueId());

        if (!preValidation(sender, targetPlayer))
            return;

        String notificationMode = getNotificationMode();

        addKudo(sender, targetPlayerUUID);
        if (sender instanceof Player)
            setCooldown(sender);

        if (!postValidation(sender, targetPlayer, notificationMode))
            return;

        if (!(sender instanceof Player)) {
            sendConsole(sender, targetPlayer, targetPlayerUUID);
            return;
        }

        if (notificationMode.equals("broadcast")) {
            sendBroadcast(sender, targetPlayer, targetPlayerUUID);
            return;
        }

        if (notificationMode.equals("private")) {
            sendPrivate(sender, targetPlayer, targetPlayerUUID);
        }

    }

    private boolean preValidation(CommandSender sender, Player targetPlayer) {
        if (!validatePlayerCooldown(sender))
            return false;

        if (!isAwardItemEnabled())
            return false;

        if (!addAwardItem(sender, targetPlayer)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.player-inventory-is-full")
                    .replaceAll("%targetplayer%", targetPlayer.getName())));

            if (sender instanceof Player)
                cooldownManager.setCooldown(((Player) sender).getUniqueId(), 0);

            return false;
        }

        return true;
    }

    private boolean postValidation(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (!isKudoAwardNotificationEnabled())
            return false;

        handlePlaySound(sender, targetPlayer, notificationMode);

        if (sendMilestone(sender, targetPlayer, targetPlayer.getUniqueId()))
            return false;

        return true;
    }

    private void addKudo(CommandSender sender, UUID targetPlayerUUID) {
        if (sender instanceof Player) {
            data.addKudos(targetPlayerUUID, ((Player) sender).getUniqueId(), 1);
        } else {
            data.addKudos(targetPlayerUUID, null, 1);
        }
    }

    private void sendConsole(CommandSender sender, Player targetPlayer, UUID targetPlayerUUID) {
        String awardMessage = locale.getString("kudo.player-award-kudo-from-console").replaceAll("%targetplayer%", targetPlayer.getName());
        awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + awardMessage));
    }

    private void sendBroadcast(CommandSender sender, Player targetPlayer, UUID targetPlayerUUID) {
        String awardMessage = locale.getString("kudo.player-award-kudo-broadcast");
        awardMessage = awardMessage.replaceAll("%player%", sender.getName());
        awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
        awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + awardMessage));
    }

    private void sendPrivate(CommandSender sender, Player targetPlayer, UUID targetPlayerUUID) {
        // Prepare message for sender
        String senderMessage = locale.getString("kudo.player-assigned-kudo").replaceAll("%targetplayer%", targetPlayer.getName());

        // Prepare message for targetplayer
        String targetPlayerMessage = locale.getString("kudo.player-award-kudo-from-player");
        targetPlayerMessage = targetPlayerMessage.replaceAll("%player%", sender.getName());
        targetPlayerMessage = targetPlayerMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));

        // Send messages
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + senderMessage));
        targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + targetPlayerMessage));
    }

    private boolean sendMilestone(CommandSender sender, Player targetPlayer, UUID targetPlayerUUID) {
        if (!isMilestone(targetPlayer)) {
            return false;
        }

        if (sender instanceof Player) {
            String awardMessage = locale.getString("milestone.player-reaches-milestone");
            awardMessage = awardMessage.replaceAll("%player%", sender.getName());
            awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
            awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + awardMessage));
            return true;
        }

        if (sender instanceof ConsoleCommandSender) {
            String awardMessage = locale.getString("milestone.player-reaches-milestone-through-console");
            awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
            awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + awardMessage));
            return true;
        }

        return false;
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.no-permission")));
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.specify-player")));
            return false;
        }
        if (args.length > 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.wrong-usage")));
            return false;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.player-not-online").replaceAll("%targetplayer%", args[0])));
            return false;
        }
        if (sender == Bukkit.getPlayer(args[0])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.cant-give-yourself-kudo")));
            return false;
        }
        return true;
    }

    private void setCooldown(CommandSender sender) {
        if (!(sender instanceof Player))
            return;

        UUID senderUUID = ((Player) sender).getUniqueId();
        cooldownManager.setCooldown(senderUUID, config.getInt("kudo-award-cooldown"));
        new BukkitRunnable() {
            @Override
            public void run() {
                int timeLeft = cooldownManager.getCooldown(senderUUID);
                cooldownManager.setCooldown(senderUUID, --timeLeft);
                if (timeLeft == 0)
                    this.cancel();
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    private boolean addAwardItem(CommandSender sender, Player targetPlayer) {
        if (!itemCanBeAdded(targetPlayer.getInventory()))
            return false;

        targetPlayer.getInventory().addItem(createAwardItem());

        return true;
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
    private boolean isAwardItemEnabled() {
        return config.getBoolean("award-item.enabled");
    }

    private boolean validatePlayerCooldown(CommandSender sender) {
        if (!canAwardKudos()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.must-wait-before-use-again")
                    .replaceAll("%seconds%", String.valueOf(timeLeft))));
            return false;
        }

        return true;
    }

    private void handlePlaySound(CommandSender sender, Player targetPlayer, String notificationMode) {
        if (isMilestoneEnabled()) {
            if (!isMilestonePlaysoundEnabled())
                return;
            if (validateMilestone(targetPlayer)) {
                playSound(sender, targetPlayer, config.getString("milestone.playsound-type"));
                return;
            }
        }

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
        if (getNotificationMode().equals("private") && !validateMilestone(targetPlayer)) {
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

    private ItemStack createAwardItem() {
        Material material = Material.getMaterial(config.getString("award-item.item"));
        String displayName = config.getString("award-item.item-name");
        List<String> lore = config.getStringList("award-item.item-lore");
        int amount = config.getInt("award-item.amount");
        boolean setLore = config.getBoolean("award-item.use-lore");
        ItemStack itemStack = new ItemCreator(material, displayName, lore, amount, setLore).create();

        return itemStack;
    }

    private boolean isMilestoneEnabled() {
        return config.getBoolean("milestone.enabled");
    }

    private boolean isMilestonePlaysoundEnabled() {
        return config.getBoolean("milestone.enable-playsound");
    }

    private boolean isKudoAwardPlaysoundEnabled() {
        return config.getBoolean("kudo-award-notification.enable-playsound");
    }

    private boolean canAwardKudos() {
        return timeLeft == 0;
    }

    private boolean isKudoAwardNotificationEnabled() {
        return config.getBoolean("kudo-award-notification.enabled");
    }

    private boolean itemCanBeAdded(Inventory inventory) {
        ItemStack awardItem = createAwardItem();
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                return true;
            }
            if (inventory.getItem(i).isSimilar(awardItem) && !(inventory.getItem(i).getAmount() + config.getInt("award-item.amount") > 64)) {
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
