package Utils.KudosUtils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public class KudosExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "kudos";
    }

    @Override
    public String getAuthor() {
        return "Urbance";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getRequiredPlugin() {
        return "Kudos";
    }

    @Override
    public String onRequest(OfflinePlayer player, String parameter) {
        if (player == null) return "KUDOS_PLACEHOLDER_PLAYER_NOT_FOUND";

        Main plugin = Main.getPlugin(Main.class);
        SQLGetter data = new SQLGetter(plugin);
        UUID playerUUID = player.getUniqueId();
        String notAssignedKudos = plugin.guiConfig.getString("slot.kudos-leaderboard.not-assigned-kudos");
        List<String> topPlayerKudos = data.getTopPlayersKudos();

        switch (parameter) {
            case "player_name" -> {
                return player.getName();
            }
            case "player_kudos" -> {
                return String.valueOf(data.getAmountKudos(playerUUID));
            }
            case "player_assigned_kudos" -> {
                return String.valueOf(data.getAssignedKudos(playerUUID));
            }
            case "top1_kudos" -> {
                if (topPlayerKudos.isEmpty())
                    return notAssignedKudos;
                return topPlayerKudos.get(0);
            }
            case "top2_kudos" -> {
                if (topPlayerKudos.size() < 2)
                    return notAssignedKudos;
                return topPlayerKudos.get(1);
            }
            case "top3_kudos" -> {
                if (topPlayerKudos.size() < 3)
                    return notAssignedKudos;
                return topPlayerKudos.get(2);
            }
        }
        return null;
    }
}
