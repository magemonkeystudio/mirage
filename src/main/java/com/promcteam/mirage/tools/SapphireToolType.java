package com.promcteam.mirage.tools;

import com.promcteam.sapphire.Sapphire;
import com.promcteam.risecore.item.DarkRiseItem;
import org.bukkit.inventory.ItemStack;

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
