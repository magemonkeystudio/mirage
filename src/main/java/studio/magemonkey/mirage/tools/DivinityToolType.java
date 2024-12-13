package studio.magemonkey.mirage.tools;

import org.bukkit.inventory.ItemStack;
import studio.magemonkey.divinity.stats.items.ItemStats;

public class DivinityToolType extends ToolType {
    public static final String PREFIX = "DIVINITY_";

    DivinityToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        String itemId = ItemStats.getId(itemStack);
        return itemId != null && itemId.equals(this.id);
    }
}
