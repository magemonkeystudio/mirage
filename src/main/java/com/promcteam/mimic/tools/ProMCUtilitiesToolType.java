package com.promcteam.mimic.tools;

import com.promcteam.sapphire.Sapphire;
import me.travja.darkrise.core.item.DarkRiseItem;
import org.bukkit.inventory.ItemStack;

public class ProMCUtilitiesToolType extends ToolType {
    public static final String PREFIX = "PROMCU_";

    ProMCUtilitiesToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        DarkRiseItem riseItem = Sapphire.getItemsRegistry().getItemByStack(itemStack);
        return riseItem != null && riseItem.getId().equals(this.id);
    }
}
