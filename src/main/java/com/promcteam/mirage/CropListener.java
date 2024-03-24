package com.promcteam.mirage;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.ArrayList;
import java.util.Arrays;

import static org.bukkit.Material.*;

public class CropListener implements Listener {

    private final ArrayList<Material> crops  = //new ArrayList<>(Arrays.asList(WHEAT, COCOA, POTATO, CARROT));
            new ArrayList<>(Arrays.asList(WHEAT, COCOA, POTATOES, BEETROOTS, CARROTS, SWEET_BERRY_BUSH));
    private final ArrayList<Material> blocks =
            new ArrayList<>(Arrays.asList(/*BAMBOO, */SUGAR_CANE, CACTUS, MELON, PUMPKIN));

    //This applies to wheat, melons, pumpkins, sugarcane, cactus, and Turtle Eggs
    @EventHandler
    public void block(BlockGrowEvent event) {

        Material block = event.getNewState().getType();

        if (block == TURTLE_EGG && Data.restrictTurtleEgg) {
            Mirage.getInstance().debug("Cancelled turtle egg");
            event.setCancelled(true);
        }
        if (crops.contains(block) && Data.restrictCropGrowth) {
            Mirage.getInstance().debug("Cancelled crop growth");
            event.setCancelled(true);
        }
        if (blocks.contains(block) && Data.restrictMelonGrowth) {
            Mirage.getInstance().debug("Cancelled melon/pumpkin/sugarcane/cactus growth");
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void spread(BlockSpreadEvent event) {
        Material mat = event.getNewState().getType();
        if ((mat == Material.BAMBOO || mat == CHORUS_PLANT/*mat == DOUBLE_PLANT*/) && Data.restrictBlockGrowth) {
            Mirage.getInstance().debug("Cancelled bamboo or chorus plant growth");
            event.setCancelled(true);
        }
    }
}
