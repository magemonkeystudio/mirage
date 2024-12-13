package studio.magemonkey.mirage.tools;

import org.bukkit.inventory.ItemStack;
import studio.magemonkey.codex.legacy.riseitem.DarkRiseItem;
import studio.magemonkey.sapphire.Sapphire;

public class SapphireToolType extends ToolType {
    public static final String PREFIX = "SAPPHIRE_";

    SapphireToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        DarkRiseItem riseItem = Sapphire.getItemsRegistry().getItemByStack(itemStack);
        return riseItem != null && riseItem.getId().equals(this.id);
    }
}
