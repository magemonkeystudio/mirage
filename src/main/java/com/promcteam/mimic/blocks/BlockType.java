package com.promcteam.mimic.blocks;

import com.promcteam.mimic.Mimic;
import com.promcteam.mimic.tools.ItemsAdderToolType;
import com.promcteam.mimic.tools.OraxenToolType;
import dev.lone.itemsadder.api.CustomBlock;
import io.th0rgal.oraxen.api.OraxenBlocks;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class BlockType {
    private static final Set<BlockType> REGISTRY = new HashSet<>();

    protected final String id;

    @Nullable
    public static BlockType make(String id, boolean warn) {
        if (id.startsWith(OraxenToolType.PREFIX)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
                invalidBlock("Ignoring block id \"" + id + "\", Oraxen is not enabled", warn);
                return null;
            }
            if (!OraxenBlocks.isOraxenBlock(id.substring(OraxenToolType.PREFIX.length()))) {
                invalidBlock("Ignoring unknown Oraxen block id \"" + id + '"', warn);
                return null;
            }
            return canonize(new OraxenBlockType(id));
        } else if (id.startsWith(ItemsAdderToolType.PREFIX)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
                invalidBlock("Ignoring block id \"" + id + "\", ItemsAdder is not enabled", warn);
                return null;
            }
            if (!CustomBlock.isInRegistry(id.substring(ItemsAdderToolType.PREFIX.length()))) {
                invalidBlock("Ignoring unknown ItemsAdder block id \"" + id + '"', warn);
                return null;
            }
            return canonize(new ItemsAdderBlockType(id));
        } else {
            try {
                return canonize(new VanillaBlockType(id));
            } catch (IllegalArgumentException e) {
                invalidBlock(e.getMessage(), warn);
                return null;
            }
        }
    }

    private static BlockType canonize(BlockType blockType) {
        Optional<BlockType> existing = REGISTRY.stream().filter(blockType1 -> blockType1.equals(blockType)).findFirst();
        if (existing.isPresent()) {
            return existing.get();
        } else {
            REGISTRY.add(blockType);
            return blockType;
        }
    }

    private static void invalidBlock(String message, boolean warn) {
        if (warn) {
            Mimic.getInstance().getLogger().warning(message);
        }
    }

    public BlockType(String fullId) {
        if (!fullId.startsWith(this.getPrefix())) {
            throw new IllegalArgumentException();
        }
        this.id = fullId.substring(this.getPrefix().length());
    }

    public abstract String getPrefix();

    public String getId() {return this.id;}

    public String getFullId() {return this.getPrefix() + this.getId();}

    public abstract boolean isInstance(Block block);

    public abstract void place(Block block);

    public void handleBreak(BlockBreakEvent event) {
        event.getBlock().breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
    }

    @Override
    public String toString() {return this.getFullId();}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockType blockType = (BlockType) o;
        return this.id.equals(blockType.id);
    }

    @Override
    public int hashCode() {return Objects.hash(this.id);}
}
