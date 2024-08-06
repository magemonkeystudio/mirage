package studio.magemonkey.mirage;

import studio.magemonkey.mirage.blocks.BlockType;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RegenerationEntry implements ConfigurationSerializable {
    private final boolean   old;
    private       Long      regenTime;
    private       BlockType blockType;
    private       Location  location;

    public RegenerationEntry(Map<?, ?> tempmap) {
        Map<String, Object> map = (Map<String, Object>) tempmap;
        map.put("yaw", Float.valueOf(0.0F));
        map.put("pitch", Float.valueOf(0.0F));
        this.location = Location.deserialize(map);
        this.blockType = BlockType.make((String) map.get("material"), false);
        this.regenTime = (Long) map.get("regenTime");
        Validate.notNull(this.blockType);
        this.old = true;
        Mirage.getInstance().debug("RegenerationEntry %s, time: " + (new Date(this.regenTime.longValue())));
    }

    public RegenerationEntry(Location location, Map.Entry<BlockType, DataEntry> entry) {
        this.location = location;
        this.blockType = entry.getKey();
        this.regenTime = System.currentTimeMillis() + entry.getValue().getRegenerationDelay().longValue() / 20L * 1000L;
        this.old = false;
    }

    public Long getRegenTime() {
        return this.regenTime;
    }

    public void setRegenTime(Long regenTime) {
        this.regenTime = regenTime;
    }

    public BlockType getBlockType() {
        return this.blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
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
        map.put("material", this.blockType.toString());
        map.putAll(this.location.serialize());
        map.remove("yaw");
        map.remove("pitch");

        return map;
    }

    public void regenerate() {
        Optional<Map.Entry<BlockType, DataEntry>> entry = Mirage.getInstance().getData().match(this.blockType);

        if (!entry.isPresent()) {
            return;
        }
        DataEntry dataEntry = entry.get().getValue();

        Mirage.getInstance().getData().getRegenerationEntries().remove(this);
        BlockType chance = dataEntry.chance();
        if (chance == null) {
            chance = this.blockType;
        }
        chance.place(this.location.getBlock());
        if (dataEntry.isAgeable() && this.location.getBlock().getBlockData() instanceof Ageable) {
            Mirage.getInstance().debug("Setting block's age to " + dataEntry.getAge());
            Ageable ageable = ((Ageable) this.location.getBlock().getBlockData());
            ageable.setAge(dataEntry.getAge());
            this.location.getBlock().setBlockData(ageable);
        }
        Mirage.getInstance().getData().getTasks().remove(getLocation());
        Mirage.getInstance().debug("Regenerated at: " + this.location);
    }
}