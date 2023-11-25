package co.marcin.darkrise.riseresources;

import co.marcin.darkrise.riseresources.blocks.BlockType;
import co.marcin.darkrise.riseresources.hooks.FactionsUUIDHook;
import co.marcin.darkrise.riseresources.hooks.LandsHook;
import co.marcin.darkrise.riseresources.hooks.TownyHook;
import co.marcin.darkrise.riseresources.hooks.WorldGuardHook;
import co.marcin.darkrise.riseresources.rewards.AmountReward;
import co.marcin.darkrise.riseresources.rewards.Reward;
import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import dev.espi.protectionstones.ProtectionStones;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
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
        Optional<Map.Entry<BlockType,DataEntry>> entry = this.plugin.getData().match(event);
   
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
        DataEntry dataEntry = entry.get().getValue();

        if (isProtected(event.getBlock())) return;

        RiseResourcesPlugin.getInstance().debug("This is a ProBlockRegen resource.");

        if (!dataEntry.isUsableTool(event.getPlayer().getInventory().getItemInMainHand())) {
            String toolMessage = dataEntry.getToolMessage();
            if (toolMessage != null) {event.getPlayer().sendMessage(toolMessage);}
            return;
        }
        RiseResourcesPlugin.getInstance().debug("We're using the correct tool.");

        if (!dataEntry.meetsSkillAPIRequirements(event.getPlayer())) return;

        AmountReward cost = dataEntry.applyCostsAndRewards(event.getPlayer(), true);
        if (cost != null) {
            RiseResourcesPlugin.getInstance().debug("Cannot afford "+cost.getAmount()+" "+cost.getName());
            return;
        }
        RiseResourcesPlugin.getInstance().debug("Mining costs deducted");

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int maxDamage = item.getType().getMaxDurability();
            if (maxDamage > 0) {
                boolean indestructible = item.getType().equals(Material.ELYTRA);
                if (indestructible) {maxDamage--;}
                int newDamage = damageable.getDamage()+dataEntry.getToolDamage();
                if (newDamage >= maxDamage) {
                    if (indestructible) {
                        RiseResourcesPlugin.getInstance().debug("Can't use item over max durability");
                        return;
                    }
                    damageable.setDamage(0);
                    item.setItemMeta(meta);
                    Player player = event.getPlayer();
                    Bukkit.getPluginManager().callEvent(new PlayerItemBreakEvent(player, item));
                    item.setAmount(item.getAmount()-1);
                    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
                } else {
                    damageable.setDamage(newDamage);
                    item.setItemMeta(meta);
                }
            }
        }
        if(!dataEntry.cancelDrop()) {
            entry.get().getKey().handleBreak(event);
        }

//        item.setDurability((short) dataEntry.get().getToolDamage().intValue());
        event.getPlayer().getInventory().setItemInMainHand(item);
        RiseResourcesPlugin.getInstance().debug("Tool has been damaged and applied. Executing any commands.");

        dataEntry.executeCommands(event.getPlayer());
        RiseResourcesPlugin.getInstance().debug("Updating the block to the break material.");
        dataEntry.getBreakMaterial().place(event.getBlock());
//        event.getBlock().setData((byte) dataEntry.get().getBreakMaterial().getDurability());
        event.getBlock().getState().update();
        RiseResourcesPlugin.getInstance().debug("Block broken and updated. Starting regeneration task.");
        this.plugin.getData().addRegenerationEntry(event.getBlock(), entry.get(), true);

        RiseResourcesPlugin.getInstance().debug(String.format("Resource broken (%s) at (%d, %d, %d) by %s",
                entry.get().getKey().toString(),
                event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ(),
                event.getPlayer().getName()));
    }

    private boolean isProtected(Block block) {
        if (this.plugin.isWorldGuardEnabled() && WorldGuardHook.isRegionDisabledAt(block.getLocation())) {
            RiseResourcesPlugin.getInstance().debug("This is in a WG disabled region.");
            return true;
        }
        if (this.plugin.isTownyHookEnabled() && TownyHook.isClaimed(block.getLocation())) {
            RiseResourcesPlugin.getInstance().debug("This is in a Towny claimed chunk.");
            return true;
        }
        if(this.plugin.isFactionsUUIDEnabled() && FactionsUUIDHook.isClaimed(block.getLocation())) {
            RiseResourcesPlugin.getInstance().debug("This is in a FactionsUUID claimed chunk.");
            return true;
        }
        if(this.plugin.isResidenceEnabled() && ResidenceApi.getResidenceManager().getByLoc(block.getLocation()) != null) {
            RiseResourcesPlugin.getInstance().debug("This is in a Residence claimed region.");
            return true;
        }
        if(this.plugin.isGriefPreventionEnabled() && GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), false, null) != null) {
            RiseResourcesPlugin.getInstance().debug("This is in a GriefPrevention claimed region.");
            return true;
        }
        if(this.plugin.isProtectionStonesEnabled() && ProtectionStones.isProtectBlock(block)) {
            RiseResourcesPlugin.getInstance().debug("This is in a ProtectionStones protected block.");
            return true;
        }
        if (this.plugin.isGriefDefenderEnabled()) {
            Claim claim = GriefDefender.getCore().getClaimAt(block.getLocation());
            if (claim != null && !claim.isWilderness()) {
                RiseResourcesPlugin.getInstance().debug("This is in a ProtectionStones protected block.");
                return true;
            }
        }
        if(this.plugin.getLandsIntegration() != null && LandsHook.isClaimed(block.getLocation())) {
            RiseResourcesPlugin.getInstance().debug("This is a Lands-Area");
            return true;
        }
        return false;
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.plugin.getData().checkChunkRegeneration(event.getChunk()), 1L);
    }
}