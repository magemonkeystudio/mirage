package co.marcin.darkrise.riseresources.tools;

import org.bukkit.inventory.ItemStack;
import su.nightexpress.quantumrpg.stats.items.ItemStats;

public class ProRPGItemsToolType extends ToolType {
    public static final String PREFIX = "PRORPGITEMS_";

    ProRPGItemsToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        String itemId = ItemStats.getId(itemStack);
        return itemId != null && itemId.equals(this.id);
    }
}
