package co.marcin.darkrise.riseresources.tools;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import com.gotofinal.darkrise.economy.DarkRiseEconomy;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import mc.promcteam.engine.modules.IModule;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class ToolType {
    private static final Set<ToolType> REGISTRY = new HashSet<>();

    protected final      String         id;


    @Nullable
    public static ToolType make(String id) {
        if (id.startsWith(ProMCUtilitiesToolType.PREFIX)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ProMCUtilities")){
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+id+"\", ProMCUtilities is not enabled");
                return null;
            }
            if (DarkRiseEconomy.getItemsRegistry().getItemById(id.substring(ProMCUtilitiesToolType.PREFIX.length())) == null) {
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown ProMCUtilities tool \""+id+'"');
                return null;
            }
            return canonize(new ProMCUtilitiesToolType(id));
        } else if (id.startsWith(OraxenToolType.PREFIX)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("Oraxen")){
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+id+"\", Oraxen is not enabled");
                return null;
            }
            if (!OraxenItems.exists(id.substring(OraxenToolType.PREFIX.length()))) {
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown Oraxen tool \""+id+'"');
                return null;
            }
            return canonize(new OraxenToolType(id));
        } else if (id.startsWith(ItemsAdderToolType.PREFIX)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")){
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+id+"\", ItemsAdder is not enabled");
                return null;
            }
            if (!CustomStack.isInRegistry(id.substring(ItemsAdderToolType.PREFIX.length()))) {
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown ItemsAdder tool \""+id+'"');
                return null;
            }
            return canonize(new ItemsAdderToolType(id));
        } else if (id.startsWith(ProRPGItemsToolType.PREFIX)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ProRPGItems")){
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+id+"\", ProRPGItems is not enabled");
                return null;
            }
            String itemId = id.substring(ProRPGItemsToolType.PREFIX.length());
            boolean found = false;
            for (IModule<?> module : QuantumRPG.getInstance().getModuleManager().getModules()) {
                if (module instanceof QModuleDrop && ((QModuleDrop<? extends ModuleItem>) module).getItemById(itemId) != null) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown ProRPGItems tool \""+id+'"');
                return null;
            }
            return canonize(new ProRPGItemsToolType(id));
        } else {
            try {
                return canonize(new VanillaToolType(Material.valueOf(id
                        .trim()
                        .toUpperCase()
                        .replace(' ', '_')
                        .replace('-', '_'))));
            } catch (IllegalArgumentException e) {
                RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown material tool \""+id+'"');
                return null;
            }
        }
    }

    private static ToolType canonize(ToolType toolType) {
        Optional<ToolType> existing = REGISTRY.stream().filter(toolType1 -> toolType1.equals(toolType)).findFirst();
        if (existing.isPresent()) {
            return existing.get();
        } else {
            REGISTRY.add(toolType);
            return toolType;
        }
    }

    public ToolType(String fullId) {
        if (!fullId.startsWith(this.getPrefix())) {throw new IllegalArgumentException();}
        this.id = fullId.substring(this.getPrefix().length());
    }

    public abstract String getPrefix();

    public String getId() {return this.id;}

    public String getFullId() {return this.getPrefix()+this.getId();}

    public abstract boolean isInstance(ItemStack itemStack);

    @Override
    public String toString() {return this.getFullId();}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {return false;}
        ToolType toolType = (ToolType) o;
        return this.id.equals(toolType.id);
    }

    @Override
    public int hashCode() {return Objects.hash(this.id);}
}
