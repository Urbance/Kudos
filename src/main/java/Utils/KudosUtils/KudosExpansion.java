package Utils.KudosUtils;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.List;

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
        SQLGetter data = new SQLGetter(Main.getPlugin(Main.class));

        if (player == null)  {
            return "KUDOS_PLACEHOLDER_PLAYER_NOT_FOUND";
        }
        if (parameter.equals("player_name"))
            return player.getName();

        if (parameter.equals("player_kudos"))
            return String.valueOf(data.getKudos(player.getUniqueId()));

        if (parameter.equals("player_assigned_kudos"))
            return String.valueOf(data.getAssignedKudo(player.getUniqueId()));

        if (parameter.equals("top1_kudos")) {
            List<String> topThree = data.getTopThreePlayers();
            return topThree.get(0);
        }

        if (parameter.equals("top2_kudos")) {
            List<String> topThree = data.getTopThreePlayers();
            return topThree.get(1);
        }

        if (parameter.equals("top3_kudos")) {
            List<String> topThree = data.getTopThreePlayers();
            return topThree.get(2);
        }

        return null;
    }
}
