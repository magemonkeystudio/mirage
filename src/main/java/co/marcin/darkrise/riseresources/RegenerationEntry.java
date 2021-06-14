package co.marcin.darkrise.riseresources;

import me.travja.darkrise.core.Debugger;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegenerationEntry implements ConfigurationSerializable {
    private final boolean old;
    private Long regenTime;
    private ItemStack material;
    private Location location;

    public RegenerationEntry(Map<?, ?> tempmap) {
        Map<String, Object> map = (Map<String, Object>) tempmap;
        map.put("yaw", Float.valueOf(0.0F));
        map.put("pitch", Float.valueOf(0.0F));
        this.location = Location.deserialize(map);
        this.material = Utils.getItemFromString((String) map.get("material"));
        this.regenTime = (Long) map.get("regenTime");
        Validate.notNull(this.material);
        this.old = true;
        RiseResourcesPlugin.getInstance().getLogger().info("RegenerationEntry %s, time: " + (new Date(this.regenTime.longValue())).toString());
    }

    public RegenerationEntry(Location location, DataEntry dataEntry) {
        this.location = location;
        this.material = dataEntry.getMaterial();
        this.regenTime = Long.valueOf(System.currentTimeMillis() + dataEntry.getRegenerationDelay().longValue() / 20L * 1000L);
        this.old = false;
    }

    public Long getRegenTime() {
        return this.regenTime;
    }

    public void setRegenTime(Long regenTime) {
        this.regenTime = regenTime;
    }

    public ItemStack getMaterial() {
        return this.material;
    }

    public void setMaterial(ItemStack material) {
        this.material = material;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isOld() {
        return this.old;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        map.put("regenTime", this.regenTime);
        map.put("material", this.material.getType().name() + ":" + this.material.getDurability());
        map.putAll(this.location.serialize());
        map.remove("yaw");
        map.remove("pitch");

        return map;
    }

    public void regenerate() {
        Optional<DataEntry> entry = RiseResourcesPlugin.getInstance().getData().match(this.material);

        if (!entry.isPresent()) {
            return;
        }

        RiseResourcesPlugin.getInstance().getData().getRegenerationEntries().remove(this);
        ItemStack chc = entry.get().chance();
        this.location.getBlock().setType(chc.getType());
        this.location.getBlock().getState().setRawData((byte) chc.getDurability());
        this.location.getBlock().getState().update();
        if (entry.get().isAgeable() && this.location.getBlock().getBlockData() instanceof Ageable) {
            Debugger.log("Setting block's age to " + entry.get().getAge());
            Ageable ageable = ((Ageable) this.location.getBlock().getBlockData());
            ageable.setAge(entry.get().getAge());
            this.location.getBlock().setBlockData(ageable);
        }
        RiseResourcesPlugin.getInstance().getData().getTasks().remove(getLocation());
        RiseResourcesPlugin.getInstance().getLogger().info("Regenerated at: " + this.location);
    }
}