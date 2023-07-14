package co.marcin.darkrise.riseresources.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class VanillaToolType extends ToolType {
    private final Material material;

    VanillaToolType(Material material) {
        super(material.name());
        this.material = material;
    }

    @Override
    public String getPrefix() {return "";}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        return itemStack.getType().equals(this.material);
    }
}
