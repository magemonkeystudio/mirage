package co.marcin.darkrise.riseresources.blocks;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import co.marcin.darkrise.riseresources.tools.OraxenToolType;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.mechanics.Mechanic;
import org.bukkit.block.Block;

public class OraxenBlockType extends BlockType {

    OraxenBlockType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return OraxenToolType.PREFIX;}

    @Override
    public boolean isInstance(Block block) {
        Mechanic mechanic = OraxenBlocks.getOraxenBlock(block.getBlockData());
        if (mechanic == null) {
            RiseResourcesPlugin.getInstance().debug("Block doesn't contain Oraxen data");
            return false;
        }
        RiseResourcesPlugin.getInstance().debug("Found Oraxen data: "+mechanic.getItemID());
        return mechanic.getItemID().equalsIgnoreCase(this.id);
    }

    @Override
    public void place(Block block) {
        OraxenBlocks.place(this.id, block.getLocation());
    }
}
