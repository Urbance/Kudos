package Utils;

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
    public String onRequest(OfflinePlayer player, String params) {
        SQLGetter sqlGetter = new SQLGetter(Main.getPlugin(Main.class));

        if (params.equals("amount")) {
            return String.valueOf(sqlGetter.getKudos(player.getUniqueId()));
        }
        if (params.equals("top1")) {
            List<String> topThree = sqlGetter.getTopThreePlayers();
            return topThree.get(0);
        }
        if (params.equals("top2")) {
            List<String> topThree = sqlGetter.getTopThreePlayers();
            return topThree.get(1);
        }
        if (params.equals("top3")) {
            List<String> topThree = sqlGetter.getTopThreePlayers();
            return topThree.get(2);
        }

        return null;
    }
    /*
     * Troubleshooting:
     * onRegister Method
     */
}
