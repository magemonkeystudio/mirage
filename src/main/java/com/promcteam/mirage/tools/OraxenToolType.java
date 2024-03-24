package com.promcteam.mirage.tools;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;

public class OraxenToolType extends ToolType {
    public static final String PREFIX = "ORAXEN_";

    OraxenToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        String itemId = OraxenItems.getIdByItem(itemStack);
        return itemId != null && itemId.equals(this.id);
    }
}
