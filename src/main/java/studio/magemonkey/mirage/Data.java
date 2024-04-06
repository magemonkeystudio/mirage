package studio.magemonkey.mirage;

import studio.magemonkey.mirage.blocks.BlockType;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class Data {
    private static final ScheduledExecutorService scheduler             = Executors.newSingleThreadScheduledExecutor();
    private static final Long                     watchdogInterval      = Long.valueOf(900L);
    private static final Long                     watchdogIntervalTicks =
            Long.valueOf(watchdogInterval.longValue() * 20L);
    public static        boolean                  restrictCropGrowth    = true, restrictMelonGrowth = true,
            restrictTurtleEgg                                           = false, restrictBlockGrowth = true;
    private final Map<BlockType, DataEntry>     entries             = new HashMap<>();
    private final Collection<RegenerationEntry> regenerationEntries = new HashSet<>();
    private final Map<Location, BukkitTask>     tasks               = new HashMap<>();
    private       File                          storageFile;
    private       BukkitTask                    watchdog            = null;

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public Collection<RegenerationEntry> getRegenerationEntries() {
        return this.regenerationEntries;
    }

    public void setStorageFile(File storageFile) {
        this.storageFile = storageFile;
    }

    public Map<Location, BukkitTask> getTasks() {
        return this.tasks;
    }

    public BukkitTask getWatchdog() {
        return this.watchdog;
    }

    public void load(ConfigurationSection section) {
        this.entries.clear();
        section.getMapList("entries").stream()
                .map(map -> {
                    try {
                        return new DataEntry((Map<String, Object>) map);
                    } catch (Exception e) {
                        Mirage.getInstance().getLogger().info("Invalid entry: " + e.getMessage());
                        e.printStackTrace();
                        return null;
                    }
                }).filter(Objects::nonNull)
                .forEach(dataEntry -> {
                    for (BlockType blockType : dataEntry.getMaterials()) {
                        DataEntry existing = this.entries.put(blockType, dataEntry);
                        Mirage.getInstance().debug("Registered entry for block type " + blockType + "'");
                        if (existing != null) {
                            Mirage.getInstance()
                                    .debug("Overriding duplicate block type '" + blockType + "'");
                        }
                    }
                });

        restrictCropGrowth = section.getBoolean("disable_crop_growth");
        restrictMelonGrowth = section.getBoolean("disable_melon_growth");
        restrictTurtleEgg = section.getBoolean("disable_turtle_egg");
        restrictBlockGrowth = section.getBoolean("disable_block_growth");

        Mirage.getInstance().getLogger().info("Loaded " + this.entries.size() + " entries.");
    }

    public Optional<Map.Entry<BlockType, DataEntry>> match(Block block) {
        for (Map.Entry<BlockType, DataEntry> entry : this.entries.entrySet()) {
            BlockType blockType = entry.getKey();
            Mirage.getInstance().debug("Comparing broken " + block.getType() + " with " + blockType);
            if (!blockType.isInstance(block)) {
                Mirage.getInstance().debug("Not a match.");
                continue;
            }

            Mirage.getInstance().debug("We have a match.");
            return Optional.of(entry);
        }

        Mirage.getInstance().debug("No matches found.");
        return Optional.empty();
    }

    public Optional<Map.Entry<BlockType, DataEntry>> match(BlockType blockType) {
        for (Map.Entry<BlockType, DataEntry> entry : this.entries.entrySet()) {
            BlockType blockType1 = entry.getKey();
            Mirage.getInstance().debug("Comparing broken " + blockType1 + " with " + blockType);
            if (!blockType1.equals(blockType)) {
                Mirage.getInstance().debug("Not a match.");
                continue;
            }

            Mirage.getInstance().debug("We have a match.");
            return Optional.of(entry);
        }

        Mirage.getInstance().debug("No matches found.");
        return Optional.empty();
    }

    public Optional<Map.Entry<BlockType, DataEntry>> match(BlockBreakEvent event) {
        Mirage.getInstance()
                .debug("Attempting to find a match for " + event.getBlock().getType() + " with data value of "
                        + event.getBlock().getData());
        return match(event.getBlock());
    }

    public RegenerationEntry addRegenerationEntry(Block block, Map.Entry<BlockType, DataEntry> entry, boolean runTask) {
        Validate.notNull(block);
        Validate.notNull(entry);
        RegenerationEntry e = new RegenerationEntry(block.getLocation(), entry);
        this.regenerationEntries.add(e);
        Mirage.getInstance()
                .debug("Will be regenerated at " + new Date(e.getRegenTime().longValue()));

        if (runTask) {
            startRegenerationTask(e);
        }

        return e;
    }

    public boolean isRegenerating(Block block) {
        return this.regenerationEntries.stream().anyMatch(e -> e.getLocation().equals(block.getLocation()));
    }


    public void loadRegenerationEntries() throws IOException {
        this.regenerationEntries.clear();

        if (!this.storageFile.exists()) {
            this.storageFile.createNewFile();
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.storageFile);
        this.regenerationEntries.addAll(configuration.getMapList("data")
                .stream()
                .map(RegenerationEntry::new)
                .collect(Collectors.toList()));
    }

    public void saveRegenerationEntries() throws IOException {
        YamlConfiguration         configuration = YamlConfiguration.loadConfiguration(this.storageFile);
        List<Map<String, Object>> list          = new ArrayList<>();
        for (RegenerationEntry regenerationEntry : this.regenerationEntries) {
            Map<String, Object> serialize = regenerationEntry.serialize();
            list.add(serialize);
        }
        configuration.set("data", list);
        configuration.save(this.storageFile);
        Mirage.getInstance().getLogger().info("Saved " + list.size() + " entries.");
    }

    public void startRegenerationTask(RegenerationEntry entry) {
        if (this.tasks.containsKey(entry.getLocation())) {
            return;
        }

        Mirage.getInstance()
                .debug(entry.getLocation().toString() + " is gonna be regenerated in " + (
                        (entry.getRegenTime().longValue() - System.currentTimeMillis()) / 1000L) + " seconds");
        this.tasks.put(entry.getLocation(),
                Bukkit.getScheduler().runTaskLater(Mirage.getInstance(), entry::regenerate, (entry
                        .getRegenTime().longValue() - System.currentTimeMillis()) / 1000L * 20L));
    }

    public void startRegenerationWatchdog() {
        if (this.watchdog != null) {
            throw new IllegalStateException("Watchdog already running");
        }

        this.watchdog = Bukkit.getScheduler().runTaskTimerAsynchronously(Mirage.getInstance(),
                () -> this.regenerationEntries.stream().filter(RegenerationEntry::isOld)
                        .peek(e -> Mirage.getInstance()
                                .debug("Watchdog: " + (System.currentTimeMillis() - e.getRegenTime()) / 1000L))
                        .filter(e -> (System.currentTimeMillis() - e.getRegenTime()) / 1000L < watchdogInterval)
                        .peek(e -> Mirage.getInstance().getLogger().info("Watchdog: " + e.getLocation()))
                        .forEach(this::startRegenerationTask), 0L, watchdogIntervalTicks);
    }

    public void stopRegenerationWatchdog() {
        if (this.watchdog == null/* || this.watchdog.isCancelled()*/) {
            throw new IllegalStateException("Watchdog is not running");
        }

        this.watchdog.cancel();
        this.watchdog = null;
    }

    public void checkChunkRegeneration(Chunk chunk) {
        this.regenerationEntries.stream()
                .filter(e -> e.getLocation().getChunk().equals(chunk))
                .filter(e -> (System.currentTimeMillis() > e.getRegenTime().longValue()))
                .forEach(this::startRegenerationTask);
    }
}