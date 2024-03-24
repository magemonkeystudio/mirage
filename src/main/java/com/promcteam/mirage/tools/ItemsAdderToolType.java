package com.promcteam.mirage.tools;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderToolType extends ToolType {
    public static final String PREFIX = "ITEMSADDER_";

    ItemsAdderToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        return customStack != null && customStack.getNamespacedID().equals(this.id);
    }
}
