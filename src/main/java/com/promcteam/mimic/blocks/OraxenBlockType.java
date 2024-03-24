package com.promcteam.mimic.blocks;

import com.promcteam.mimic.Mimic;
import com.promcteam.mimic.tools.OraxenToolType;
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
            Mimic.getInstance().debug("Block doesn't contain Oraxen data");
            return false;
        }
        Mimic.getInstance().debug("Found Oraxen data: " + mechanic.getItemID());
        return mechanic.getItemID().equalsIgnoreCase(this.id);
    }

    @Override
    public void place(Block block) {
        OraxenBlocks.place(this.id, block.getLocation());
    }
}
