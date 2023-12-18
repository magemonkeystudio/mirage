package co.marcin.darkrise.riseresources.hooks;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;

public class LandsHook {

    public static boolean isClaimed(Location location){

        if(RiseResourcesPlugin.getInstance().getLandsIntegration() == null)return false;

        Area area = RiseResourcesPlugin.getInstance().getLandsIntegration().getAreaByLoc(location);

        return area != null;
    }
}
