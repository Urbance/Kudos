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
        if (player == null)  {
            return "KUDOS_PLACEHOLDER_PLAYER_NOT_FOUND";
        }

        SQLGetter data = new SQLGetter(Main.getPlugin(Main.class));
        UUID playerUUID = player.getUniqueId();

        switch (parameter) {
            case "player_name" -> {
                return player.getName();
            }
            case "player_kudos" -> {
                return String.valueOf(data.getKudos(playerUUID));
            }
            case "player_assigned_kudos" -> {
                return String.valueOf(data.getAssignedKudo(playerUUID));
            }
            case "top1_kudos" -> {
                List<String> topThree = data.getTopThreePlayers();
                return topThree.get(0);
            }
            case "top2_kudos" -> {
                List<String> topThree = data.getTopThreePlayers();
                return topThree.get(1);
            }
            case "top3_kudos" -> {
                List<String> topThree = data.getTopThreePlayers();
                return topThree.get(2);
            }
        }
        return null;
    }
}
