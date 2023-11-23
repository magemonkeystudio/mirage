package co.marcin.darkrise.riseresources.hooks;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;

public class WorldGuardHook {
    public static boolean isRegionDisabledAt(Location location) {
        WorldGuardPlatform platform = WorldGuard.getInstance().getPlatform();

        RegionManager manager = /*WorldGuardPlugin.inst().getRegionContainer().get(location.getWorld());*/platform.getRegionContainer().get(platform.getMatcher().getWorldByName(location.getWorld().getName()));
        if(manager == null)return false;
        boolean matchFound = manager
                .getApplicableRegions(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                .getRegions()
                .stream()
                .map(ProtectedRegion::getId)
                .anyMatch(RiseResourcesPlugin.getInstance().getDisabledRegions()::contains);
        return (RiseResourcesPlugin.getInstance().areRegionsBlacklisted()) ? matchFound : !matchFound;
    }
}