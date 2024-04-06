package studio.magemonkey.mirage;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import studio.magemonkey.mirage.blocks.BlockType;
import studio.magemonkey.mirage.hooks.FactionsUUIDHook;
import studio.magemonkey.mirage.hooks.LandsHook;
import studio.magemonkey.mirage.hooks.TownyHook;
import studio.magemonkey.mirage.hooks.WorldGuardHook;
import studio.magemonkey.mirage.rewards.AmountReward;
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
    private final Mirage plugin = Mirage.getInstance();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        boolean matchFound = this.plugin.getDisabledWorlds().contains(event.getBlock().getWorld().getName());
        if (this.plugin.areWorldsBlacklisted() == matchFound) {
            return;
        }

        if (event.getPlayer().hasPermission("mirage.override")) return;

        Mirage.getInstance().debug(event.getBlock().getType() + " has been broken.");
        Optional<Map.Entry<BlockType, DataEntry>> entry = this.plugin.getData().match(event);

        event.setCancelled(true);

        if (this.plugin.getData().isRegenerating(event.getBlock())) {
//            event.setCancelled(true);
            Mirage.getInstance().debug("That block is already regenerating");
            return;
        }
        if (!entry.isPresent()) {
            Mirage.getInstance().debug("Entry is not present.");
            return;
        }
        DataEntry dataEntry = entry.get().getValue();

        if (isProtected(event.getBlock())) return;

        Mirage.getInstance().debug("This is a Mirage resource.");

        if (!dataEntry.isUsableTool(event.getPlayer().getInventory().getItemInMainHand())) {
            String toolMessage = dataEntry.getToolMessage();
            if (toolMessage != null) {
                event.getPlayer().sendMessage(toolMessage);
            }
            return;
        }
        Mirage.getInstance().debug("We're using the correct tool.");

        if (!dataEntry.meetsRequirements(event.getPlayer())) return;

        AmountReward cost = dataEntry.applyCostsAndRewards(event.getPlayer(), true);
        if (cost != null) {
            Mirage.getInstance().debug("Cannot afford " + cost.getAmount() + " " + cost.getName());
            return;
        }
        Mirage.getInstance().debug("Mining costs deducted");

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta  meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable damageable = (Damageable) meta;
            int        maxDamage  = item.getType().getMaxDurability();
            if (maxDamage > 0) {
                boolean indestructible = item.getType().equals(Material.ELYTRA);
                if (indestructible) {
                    maxDamage--;
                }
                int newDamage = damageable.getDamage() + dataEntry.getToolDamage();
                if (newDamage >= maxDamage) {
                    if (indestructible) {
                        Mirage.getInstance().debug("Can't use item over max durability");
                        return;
                    }
                    damageable.setDamage(0);
                    item.setItemMeta(meta);
                    Player player = event.getPlayer();
                    Bukkit.getPluginManager().callEvent(new PlayerItemBreakEvent(player, item));
                    item.setAmount(item.getAmount() - 1);
                    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1, 1);
                } else {
                    damageable.setDamage(newDamage);
                    item.setItemMeta(meta);
                }
            }
        }
        if (!dataEntry.cancelDrop()) {
            entry.get().getKey().handleBreak(event);
        }

//        item.setDurability((short) dataEntry.get().getToolDamage().intValue());
        event.getPlayer().getInventory().setItemInMainHand(item);
        Mirage.getInstance().debug("Tool has been damaged and applied. Executing any commands.");

        dataEntry.executeCommands(event.getPlayer());
        Mirage.getInstance().debug("Updating the block to the break material.");
        dataEntry.getBreakMaterial().place(event.getBlock());
//        event.getBlock().setData((byte) dataEntry.get().getBreakMaterial().getDurability());
        event.getBlock().getState().update();
        Mirage.getInstance().debug("Block broken and updated. Starting regeneration task.");
        this.plugin.getData().addRegenerationEntry(event.getBlock(), entry.get(), true);

        Mirage.getInstance().debug(String.format("Resource broken (%s) at (%d, %d, %d) by %s",
                entry.get().getKey().toString(),
                event.getBlock().getX(),
                event.getBlock().getY(),
                event.getBlock().getZ(),
                event.getPlayer().getName()));
    }

    private boolean isProtected(Block block) {
        if (this.plugin.isWorldGuardEnabled() && WorldGuardHook.isRegionDisabledAt(block.getLocation())) {
            Mirage.getInstance().debug("This is in a WG disabled region.");
            return true;
        }
        if (this.plugin.isTownyHookEnabled() && TownyHook.isClaimed(block.getLocation())) {
            Mirage.getInstance().debug("This is in a Towny claimed chunk.");
            return true;
        }
        if (this.plugin.isFactionsUUIDEnabled() && FactionsUUIDHook.isClaimed(block.getLocation())) {
            Mirage.getInstance().debug("This is in a FactionsUUID claimed chunk.");
            return true;
        }
        if (this.plugin.isResidenceEnabled()
                && ResidenceApi.getResidenceManager().getByLoc(block.getLocation()) != null) {
            Mirage.getInstance().debug("This is in a Residence claimed region.");
            return true;
        }
        if (this.plugin.isGriefPreventionEnabled()
                && GriefPrevention.instance.dataStore.getClaimAt(block.getLocation(), false, null) != null) {
            Mirage.getInstance().debug("This is in a GriefPrevention claimed region.");
            return true;
        }
        if (this.plugin.isProtectionStonesEnabled() && ProtectionStones.isProtectBlock(block)) {
            Mirage.getInstance().debug("This is in a ProtectionStones protected block.");
            return true;
        }
        if (this.plugin.isGriefDefenderEnabled()) {
            Claim claim = GriefDefender.getCore().getClaimAt(block.getLocation());
            if (claim != null && !claim.isWilderness()) {
                Mirage.getInstance().debug("This is in a ProtectionStones protected block.");
                return true;
            }
        }
        if (this.plugin.getLandsIntegration() != null && LandsHook.isClaimed(block.getLocation())) {
            Mirage.getInstance().debug("This is a Lands-Area");
            return true;
        }
        return false;
    }


    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Bukkit.getScheduler()
                .runTaskLater(this.plugin, () -> this.plugin.getData().checkChunkRegeneration(event.getChunk()), 1L);
    }
}