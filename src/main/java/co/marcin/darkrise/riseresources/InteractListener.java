package co.marcin.darkrise.riseresources;

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

        RiseResourcesPlugin.getInstance().debug(event.getBlock().getType() + " has been broken.");
        Optional<DataEntry> entry = this.plugin.getData().match(event);
   
        event.setCancelled(true);
      
       
        if (this.plugin.getData().isRegenerating(event.getBlock())) {
//            event.setCancelled(true);
            RiseResourcesPlugin.getInstance().debug("That block is already regenerating");
            return;
        }
        if (!entry.isPresent()) {
            RiseResourcesPlugin.getInstance().debug("Entry is not present.");
            return;
        }


        if (this.plugin.isWorldGuardEnabled() && WorldGuardHook.isRegionDisabledAt(event.getBlock().getLocation())) {
            RiseResourcesPlugin.getInstance().debug("This is in a WG disabled region.");
            return;
        }


        if (this.plugin.isTownyHookEnabled() && TownyHook.isClaimed(event.getBlock().getLocation())) {
            RiseResourcesPlugin.getInstance().debug("This is in a Towny claimed chunk.");
            return;
        }

        if(this.plugin.isFactionsUUIDEnabled() && FactionsUUIDHook.isClaimed(event.getBlock().getLocation())){
            RiseResourcesPlugin.getInstance().debug("This is in a FactionsUUID claimed chunk.");
            return;
        }

        if(this.plugin.getLandsIntegration() != null && LandsHook.isClaimed(event.getBlock().getLocation())){
            RiseResourcesPlugin.getInstance().debug("This is a Lands-Area");
            return;
        }

        RiseResourcesPlugin.getInstance().debug("This is a ProBlockRegen resource.");

        if (!entry.get().isUsableTool(event.getPlayer().getInventory().getItemInMainHand())) {
            String toolMessage = entry.get().getToolMessage();
            if (toolMessage != null) {event.getPlayer().sendMessage(toolMessage);}
            return;
        }
        RiseResourcesPlugin.getInstance().debug("We're using the correct tool.");
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(event.getPlayer().getInventory().getItemInMainHand().getType());
        }
        if(entry.get().cancelDrop()) {
        	event.getBlock().breakNaturally(item);
        }

        ((Damageable) meta).setDamage(entry.get().getToolDamage());
        item.setItemMeta(meta);
//        item.setDurability((short) entry.get().getToolDamage().intValue());
        event.getPlayer().getInventory().setItemInMainHand(item);
        RiseResourcesPlugin.getInstance().debug("Tool has been damaged and applied. Executing any commands.");

        entry.get().executeCommands(event.getPlayer());
        RiseResourcesPlugin.getInstance().debug("Updating the block to the break material.");
        event.getBlock().setType(entry.get().getBreakMaterial().getType());
//        event.getBlock().setData((byte) entry.get().getBreakMaterial().getDurability());
        event.getBlock().getState().update();
        RiseResourcesPlugin.getInstance().debug("Block broken and updated. Starting regeneration task.");
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