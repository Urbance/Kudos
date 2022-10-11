package Utils;

import de.urbance.Main;
import org.bukkit.configuration.file.FileConfiguration;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private static FileConfiguration config = Main.getPlugin(Main.class).getConfig();
    public static final int COOLDOWN = config.getInt("kudo_award_cooldown");

    public void setCooldown(UUID player, int time){
        if(time < 1) {
            cooldowns.remove(player);
        } else {
            cooldowns.put(player, time);
        }
    }

    public int getCooldown(UUID player){
        return cooldowns.getOrDefault(player, 0);
    }
}