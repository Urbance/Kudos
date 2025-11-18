package Utils;

import de.urbance.Main;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class ItemCreator {

    private ItemStack itemStack;
    private ItemMeta itemMeta;
    private int amount;
    Main plugin = Main.getPlugin(Main.class);

    public ItemCreator(String material) {
        this.itemStack = new ItemStack(Material.valueOf(material));
        this.itemMeta = itemStack.getItemMeta();
        this.amount = 1;
    }

    public ItemCreator setDisplayName(String displayName) {
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        return this;
    }

    public ItemCreator setAmount(int amount) {
        this.amount = amount;
        itemStack.setAmount(this.amount);
        return this;
    }

    public ItemCreator setLore(List<String> lore) {
        lore.replaceAll(s -> ChatColor.translateAlternateColorCodes('&', s));
        itemMeta.setLore(lore);
        return this;
    }

    public ItemCreator replaceSkullWithPlayerSkull(OfflinePlayer offlinePlayer) {
        if (!(itemStack.getType() == Material.PLAYER_HEAD)) return this;

        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        try {
            if (offlinePlayer.hasPlayedBefore())
                skullMeta.setOwningPlayer(offlinePlayer);
        } catch (NoSuchElementException ignored) {

        }

        skullMeta.setDisplayName(itemMeta.getDisplayName());
        skullMeta.setLore(itemMeta.getLore());

        this.itemMeta = skullMeta;

        return this;
    }

    public ItemCreator replaceSkullWithCustomURLSkull(String url) {
        if (!(itemStack.getType() == Material.PLAYER_HEAD)) return this;

        SkullMeta skullMeta = (SkullMeta) itemMeta;

        UUID uuid = UUID.randomUUID();

        PlayerProfile playerProfile =  Bukkit.createPlayerProfile(uuid, uuid.toString().substring(0, 16));
        PlayerTextures playerTextures = playerProfile.getTextures();
        URL urlObject;

        try {
            playerTextures.setSkin(new URL(url));
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            plugin.getLogger().warning("An error occurred! Please report the error to the plugin developer:");
            e.printStackTrace();
            return this;
        }

        playerTextures.setSkin(urlObject);
        playerProfile.setTextures(playerTextures);
        skullMeta.setOwnerProfile(playerProfile);

        this.itemMeta = skullMeta;

        return this;
    }

    public ItemStack get() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
