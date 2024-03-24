package com.promcteam.mimic.hooks;

import com.promcteam.mimic.Mimic;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;

public class LandsHook {

    public static boolean isClaimed(Location location) {

        if (Mimic.getInstance().getLandsIntegration() == null) return false;

        Area area = Mimic.getInstance().getLandsIntegration().getAreaByLoc(location);

        return area != null;
    }
}
