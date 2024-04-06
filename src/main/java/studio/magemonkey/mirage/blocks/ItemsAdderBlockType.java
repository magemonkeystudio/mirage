package studio.magemonkey.mirage.blocks;

import studio.magemonkey.mirage.Mirage;
import studio.magemonkey.mirage.tools.ItemsAdderToolType;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemsAdderBlockType extends BlockType {

    ItemsAdderBlockType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return ItemsAdderToolType.PREFIX;}

    @Override
    public boolean isInstance(Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) {
            Mirage.getInstance().debug("Block doesn't contain ItemsAdder data");
            return false;
        }
        Mirage.getInstance().debug("Found ItemsAdder data: " + customBlock.getNamespacedID());
        return customBlock.getNamespacedID().equalsIgnoreCase(this.id);
    }

    @Override
    public void place(Block block) {
        CustomBlock.place(this.id, block.getLocation());
    }

    @Override
    public void handleBreak(BlockBreakEvent event) {
        Block       block       = event.getBlock();
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        if (customBlock == null) super.handleBreak(event);
        else {
            World    world    = block.getWorld();
            Location location = event.getBlock().getLocation();

            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            ItemMeta  meta      = itemStack.getItemMeta();
            if (meta != null && meta.hasEnchant(Enchantment.SILK_TOUCH)) {
                world.dropItemNaturally(location, customBlock.getItemStack());
            } else {
                for (ItemStack drop : customBlock.getLoot(itemStack, true)) {
                    world.dropItemNaturally(location, drop);
                }
            }
        }
    }
}
