package Events;

import Utils.SQL.SQLGetter;
import de.urbance.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {
    SQLGetter data;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        data = new SQLGetter(Main.getPlugin(Main.class));
        data.createPlayer(player);
    }
}
