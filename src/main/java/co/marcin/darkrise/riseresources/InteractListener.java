package co.marcin.darkrise.riseresources;

import me.travja.darkrise.core.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public class InteractListener implements Listener {
    private final RiseResourcesPlugin plugin = RiseResourcesPlugin.getInstance();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        boolean matchFound = this.plugin.getDisabledWorlds().contains(event.getBlock().getWorld().getName());
        if ((this.plugin.areWorldsBlacklisted()) ? matchFound : !matchFound) {
            return;
        }

        if (event.getPlayer().hasPermission("blockregen.override")) return;

        Debugger.log(event.getBlock().getType() + " has been broken.");
        Optional<DataEntry> entry = this.plugin.getData().match(event);
   
        event.setCancelled(true);
      
       
        if (this.plugin.getData().isRegenerating(event.getBlock())) {
//            event.setCancelled(true);
            Debugger.log("That block is already regenerating");
            return;
        }
        if (!entry.isPresent()) {
            Debugger.log("Entry is not present.");
            return;
        }


        if (this.plugin.isWorldGuardEnabled() && WorldGuardHook.isRegionDisabledAt(event.getBlock().getLocation())) {
            Debugger.log("This is in a WG disabled region.");
            return;
        }


        if (this.plugin.isTownyHookEnabled() && TownyHook.isClaimed(event.getBlock().getLocation())) {
            Debugger.log("This is in a Towny claimed chunk.");
            return;
        }

        Debugger.log("This is a ProBlockRegen resource.");

        if (!entry.get().isUsableTool(event.getPlayer().getInventory().getItemInMainHand())) {
            event.getPlayer().sendMessage(entry.get().getToolMessage());
            return;
        }
        Debugger.log("We're using the correct tool.");
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(event.getPlayer().getInventory().getItemInMainHand().getType());
        }
        if(entry.get().cancelDrop()) {
        	event.getBlock().breakNaturally(item);
        }

        ((Damageable) meta).setDamage(entry.get().getToolDamage().intValue());
        item.setItemMeta(meta);
//        item.setDurability((short) entry.get().getToolDamage().intValue());
        event.getPlayer().getInventory().setItemInMainHand(item);
        Debugger.log("Tool has been damaged and applied. Executing any commands.");

        entry.get().executeCommands(event.getPlayer());
        Debugger.log("Updating the block to the break material.");
        event.getBlock().setType(entry.get().getBreakMaterial().getType());
//        event.getBlock().setData((byte) entry.get().getBreakMaterial().getDurability());
        event.getBlock().getState().update();
        Debugger.log("Block broken and updated. Starting regeneration task.");
        this.plugin.getData().addRegenerationEntry(event.getBlock(), entry.get(), true);

        RiseResourcesPlugin.getInstance().getLogger().info(String.format("Resource broken (%s) at (%d, %d, %d) by %s",
                entry.get().getMaterial().getType().name(),
                Integer.valueOf(event.getBlock().getX()),
                Integer.valueOf(event.getBlock().getY()),
                Integer.valueOf(event.getBlock().getZ()),
                event.getPlayer().getName()));
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.plugin.getData().checkChunkRegeneration(event.getChunk()), 1L);
    }
}