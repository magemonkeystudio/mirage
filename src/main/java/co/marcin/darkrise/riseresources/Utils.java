package co.marcin.darkrise.riseresources;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Utils {

    public static ItemStack getItemFromString(String str) {
        String matStr = str;
        short data = 0;
        if(str.contains(":")) {
            matStr = str.split(":")[0];
            data = Short.parseShort(str.split(":")[1]);
        }

        Material mat = Material.matchMaterial(matStr);

        if(mat == null) {
            System.out.println("Could not find material: " + matStr);
            return null;
        }

        return new ItemStack(mat, 1, data);
    }

}
