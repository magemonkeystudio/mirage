package co.marcin.darkrise.riseresources.blocks;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import co.marcin.darkrise.riseresources.tools.ItemsAdderToolType;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.block.Block;

public class ItemsAdderBlockType extends BlockType {

    ItemsAdderBlockType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return ItemsAdderToolType.PREFIX;}

    @Override
    public boolean isInstance(Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) {
            RiseResourcesPlugin.getInstance().debug("Block doesn't contain ItemsAdder data");
            return false;
        }
        RiseResourcesPlugin.getInstance().debug("Found ItemsAdder data: "+customBlock.getNamespacedID());
        return customBlock.getNamespacedID().equalsIgnoreCase(this.id);
    }

    @Override
    public void place(Block block) {
        CustomBlock.place(this.id, block.getLocation());
    }
}
