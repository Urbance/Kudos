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

        addKudo(sender, args);

        return false;
    }

    private void addKudo(CommandSender sender, String[] args) {
        data = new SQLGetter(plugin);
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        UUID targetPlayerUUID = targetPlayer.getUniqueId();

        // TODO Clean Code -> PlaceholderAPI? | Refactoring!
        if (sender instanceof ConsoleCommandSender) {
            String awardMessage = locale.getString("kudo.player-award-kudo-from-console").replaceAll("%targetplayer%", targetPlayer.getName());
            awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + awardMessage));
            data.addKudos(targetPlayerUUID, null, 1);
            playSound(config.getString("kudo-award-notification.playsound-type"));

            // Added for patch 1.4.1 -> no longer exists in 1.5.0
            if (!validateAwardItem(sender, targetPlayer))
                return;

            return;
        }

        UUID senderUUID = ((Player) sender).getPlayer().getUniqueId();
        timeLeft = cooldownManager.getCooldown(senderUUID);

        if (!validatePlayerCooldown(sender))
            return;
        if (!validateAwardItem(sender, targetPlayer))
            return;

        setCooldown(senderUUID);
        data.addKudos(targetPlayerUUID, senderUUID, 1);

        if (validateMilestone(targetPlayer)) {
            String awardMessage = locale.getString("milestone.player-reaches-milestone");
            awardMessage = awardMessage.replaceAll("%player%", sender.getName());
            awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
            awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + awardMessage));
            playSound(config.getString("milestone.playsound-type"));
            return;
        }

        playSound(config.getString("kudo-award-notification.playsound-type"));
        String awardMessage = locale.getString("kudo.player-award-kudo");
        awardMessage = awardMessage.replaceAll("%player%", sender.getName());
        awardMessage = awardMessage.replaceAll("%targetplayer%", targetPlayer.getName());
        awardMessage = awardMessage.replaceAll("%player_kudos%", String.valueOf(data.getKudos(targetPlayerUUID)));
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',prefix + awardMessage));
    }

    private boolean validateInput(String[] args, CommandSender sender) {
        if (!(sender.hasPermission("kudos.award") || sender.hasPermission("kudos.*"))) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.no-permission")));
            return false;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.specify-player")));
            return false;
        }
        if (args.length > 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + locale.getString("error.wrong-usage")));
            return false;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.player-not-online").replaceAll("%targetplayer%", args[0])));
            return false;
        }
        if (sender == Bukkit.getPlayer(args[0])) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + locale.getString("error.cant-give-yourself-kudo")));
            return false;
        }
        return true;
    }

    private void setCooldown(UUID senderUUID) {
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

    private boolean validateAwardItem(CommandSender sender, Player targetPlayer) {
        Inventory targetPlayerInventory = targetPlayer.getInventory();

        if (isAwardItem()) {
            if (!itemCanBeAdded(targetPlayerInventory)){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +  locale.getString("error.player-inventory-is-full")
                        .replaceAll("%targetplayer%", targetPlayer.getName())));
                if (sender instanceof Player)
                    cooldownManager.setCooldown(((Player) sender).getPlayer().getUniqueId(), 0);
                return false;
            }
            targetPlayerInventory.addItem(createAwardItem());
        }
        return true;
    }

    private boolean validatePlayerCooldown(CommandSender sender) {
        if (!canAwardKudos()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix +  locale.getString("error.must-wait-before-use-again")
                    .replaceAll("%seconds%", String.valueOf(timeLeft))));
            return false;
        }
        return true;
    }

    private void playSound(String playSoundType) {
        if (config.getBoolean("kudo-award-notification.playsound-on-kudo-award")) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                try {
                    players.playSound(players, Sound.valueOf(playSoundType), 1, 1);
                }
                catch (Exception e) {
                    Bukkit.getLogger().warning("Error in the config: playsound-type \"" + playSoundType + "\" isn't a valid playsound!");
                    return;
                }
            }
        }
    }

    private boolean validateMilestone(Player targetPlayer) {
        int targetPlayerKudos = data.getKudos(targetPlayer.getUniqueId());
        if (!config.getBoolean("milestone.enabled")) {
            return false;
        }
        return targetPlayerKudos % config.getInt("milestone.span-between-kudos") == 0;
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

    private boolean canAwardKudos() {
        return timeLeft == 0;
    }

    private boolean isAwardItem() {
        return config.getBoolean("award-item.enabled");
    }

    private boolean itemCanBeAdded(Inventory inventory) {
        ItemStack awardItem = createAwardItem();
        for (int i = 0; i < 36; i++) {
            if (inventory.getItem(i) == null) {
                return true;
            }
            if (inventory.getItem(i).isSimilar(awardItem)) {
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
