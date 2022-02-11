package co.marcin.darkrise.riseresources;

import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;

public class LandsHook {

    public static boolean isClaimed(Location location){

        if(RiseResourcesPlugin.getInstance().getLandsIntegration() == null)return false;

        Area area = RiseResourcesPlugin.getInstance().getLandsIntegration().getAreaByLoc(location);

        return area != null;
    }
}
