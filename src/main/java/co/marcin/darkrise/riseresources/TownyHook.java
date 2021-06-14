package co.marcin.darkrise.riseresources;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;

public class TownyHook {
    public static boolean isClaimed(Location location) {
        try {
            TownBlock townBlock = TownyAPI.getInstance().getTownBlock(location);
            return (townBlock != null && townBlock.getTown() != null);
        } catch (NotRegisteredException e) {
            return false;
        }
    }
}