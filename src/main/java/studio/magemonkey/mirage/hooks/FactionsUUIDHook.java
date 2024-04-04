package studio.magemonkey.mirage.hooks;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import org.bukkit.Location;

public class FactionsUUIDHook {

    public static boolean isClaimed(Location location) {

        FLocation fLocation = new FLocation(location);
        return Board.getInstance().getFactionAt(fLocation).isNormal();

    }
}
