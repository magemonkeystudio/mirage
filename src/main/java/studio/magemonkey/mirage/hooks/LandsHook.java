package studio.magemonkey.mirage.hooks;

import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import studio.magemonkey.mirage.Mirage;

public class LandsHook {

    public static boolean isClaimed(Location location) {

        if (Mirage.getInstance().getLandsIntegration() == null) return false;

        Area area = Mirage.getInstance().getLandsIntegration().getAreaByLoc(location);

        return area != null;
    }
}
