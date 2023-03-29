package Utils;

import de.urbance.Main;
import org.apache.commons.lang3.time.DateUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimitationManager {

    private final Map<UUID, Integer> receivedKudos = new HashMap<>();
    private final Map<UUID, Date> receivedKudosTime = new HashMap<>();
    FileConfiguration config;
    UUID targetPlayerUUID;

    public boolean setLimitation(CommandSender sender, Player targetPlayer) {
        Main plugin = Main.getPlugin(Main.class);
        this.config = plugin.config;
        this.targetPlayerUUID = targetPlayer.getUniqueId();
        KudosMessage kudosMessage = new KudosMessage(plugin);
        FileConfiguration localeConfig = plugin.localeConfig;
        Date currentDate = new Date();

        if (!receivedKudos.containsKey(targetPlayerUUID)) {
            initFirstKudo();
            return true;
        }
        if (currentDate.after(receivedKudosTime.get(targetPlayerUUID))) {
            clearLimitation();
            initFirstKudo();
            return true;
        }
        if (receivedKudos.get(targetPlayerUUID).equals(config.getInt("kudo-award.limitation.max-kudos-to-receive"))) {
            Map<String, String> values = new HashMap<>();
            values.put("kudos_targetplayer_name", targetPlayer.getName());
            kudosMessage.sendSender(sender, kudosMessage.setPlaceholders(localeConfig.getString("error.player-received-too-many-kudos-last-time"), values));
            return false;
        }
        addReceivedKudo();
        return true;
    }

    private void addReceivedKudo() {
        receivedKudos.put(targetPlayerUUID, receivedKudos.get(targetPlayerUUID) + 1);
    }

    private void initFirstKudo() {
        Date currDate = new Date();
        receivedKudosTime.put(targetPlayerUUID, DateUtils.addSeconds(currDate, config.getInt("kudo-award.limitation.cooldown")));
        receivedKudos.put(targetPlayerUUID, 1);
    }

    private void clearLimitation() {
        receivedKudos.remove(targetPlayerUUID);
        receivedKudosTime.remove(targetPlayerUUID);
    }
}
