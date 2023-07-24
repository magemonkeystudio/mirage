package co.marcin.darkrise.riseresources;

import co.marcin.darkrise.riseresources.blocks.BlockType;
import co.marcin.darkrise.riseresources.rewards.Reward;
import co.marcin.darkrise.riseresources.tools.ToolType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class DataEntry {
    private final Set<BlockType> materials = new HashSet<>();
    private final BlockType         breakMaterial;
    private final Long                   regenerationDelay;
    private final Set<ToolType>             tools   = new HashSet<>();
    private final List<Reward>              rewards = new ArrayList<>();
    private final List<Reward>              costs   = new ArrayList<>();
    private final TreeMap<Double,BlockType> chance  = new TreeMap<>();
    private final double totalWeight;
    private final List<DataEntry.Command> commands = new ArrayList();
    private String toolMessage;
    private int toolDamage = 0;
    private Integer age;
    private boolean cancelDrop;
    
    public DataEntry(Map<String, Object> map) {
        Validate.notNull(map);
        Object object = map.getOrDefault("materials", map.get("material"));
        if (object instanceof List) {
            List<?> materialsList = (List<?>) object;
            for (Object obj : materialsList) {
                if (obj instanceof String) {
                    BlockType blockType = BlockType.make((String) obj, true);
                    if (blockType != null) {this.materials.add(blockType);}
                }
            }
        } else if (object instanceof String) {
            BlockType blockType = BlockType.make((String) object, true);
            if (blockType != null) {this.materials.add(blockType);}
        }
        if (this.materials.isEmpty()) {
            throw new IllegalArgumentException("At least one material is required");
        }

        object = map.get("break-material");
        if (object instanceof String) {
            this.breakMaterial = BlockType.make((String) object, false);
        } else {
            this.breakMaterial = null;
        }
        if (this.breakMaterial == null) {
            throw new IllegalArgumentException("Invalid 'break-material' value: "+object);
        }

        this.cancelDrop = (boolean)map.get("cancel-drop");
        this.regenerationDelay = (long) (Integer) map.get("regen-delay");
        if (map.containsKey("tool")) {
            Validate.isTrue(map.get("tool") instanceof Map, "'tool' must be a section.");
            Map<String, Object> tools = (Map) map.get("tool");
            if (tools.containsKey("allowed")) {
                List<String> stringList;
                Object allowedObject = tools.get("allowed");
                if (allowedObject instanceof String) {
                    stringList = new ArrayList<>();
                    stringList.add((String) allowedObject);
                } else {
                    if (!(allowedObject instanceof List)) {throw new IllegalArgumentException("Invalid data type.");}
                    stringList = (List<String>) allowedObject;
                }

                for (String toolString : stringList) {
                    ToolType toolType = ToolType.make(toolString);
                    if (toolType != null) {this.tools.add(toolType);}
                }
            }

            this.toolMessage = ChatColor.translateAlternateColorCodes('&', (String) tools.getOrDefault("message", null));
            this.toolDamage = (Integer) tools.getOrDefault("damage", 0);
        }

        double totalWeight = 0;
        object = map.get("chance");
        if (object instanceof Map) {
            Map<?,?> chanceMap = (Map<?,?>) object;
            for (Map.Entry<?,?> entry : chanceMap.entrySet()) {
                Object key = entry.getKey();
                BlockType blockType;
                if (key instanceof String) {
                    blockType = BlockType.make((String) key, true);
                    if (blockType == null) {continue;}
                } else {
                    throw new IllegalArgumentException("Ignoring invalid 'chance' key: "+key);
                }
                Object weight = entry.getValue();
                double chance;
                if (weight instanceof Number) {
                    chance = ((Number) weight).doubleValue();
                    if (chance == 0) {
                        RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring 'chance' key with 0 weight: "+key);
                        continue;
                    }
                } else {
                    RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring invalid 'chance' weight: "+weight);
                    continue;
                }
                this.chance.put(totalWeight+=chance, blockType);
            }
        } else if (object != null) {
            throw new IllegalArgumentException("Invalid 'chance' section: "+object);
        }
        this.totalWeight = totalWeight;

        object = map.get("rewards");
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            for (Object obj : list) {
                if (obj instanceof String) {
                    try {
                        Reward reward = Reward.make((String) obj);
                        if (reward.getAmount() > 0) {
                            this.rewards.add(reward);
                        } else {
                            this.costs.add(reward);
                        }
                    } catch (IllegalArgumentException | IllegalStateException e) {
                        RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring invalid reward/cost \""+obj+"\": "+e.getMessage());
                    } catch (Exception e) {
                        RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring invalid reward/cost \""+obj+"\":");
                        e.printStackTrace();
                    }
                }
            }
        }

        if (map.containsKey("command")) {
            Validate.isTrue(map.get("command") instanceof List, "'command' section must be a list");
            List<Map<String, Object>> commandList = (List) map.get("command");
            this.commands.addAll(commandList.stream().map(Command::new).collect(Collectors.toList()));
        }

        if (map.containsKey("age")) {
            age = (int) map.get("age");
        }

        RiseResourcesPlugin.getInstance().debug("Created DataEntry for materials:");
        for (BlockType blockType : this.materials) {RiseResourcesPlugin.getInstance().debug("- "+blockType.getPrefix());}
    }

    public boolean isUsableTool(ItemStack itemStack) {
        if (this.tools.isEmpty()) {return true;}
        for (ToolType toolType : this.tools) {
            if (toolType.isInstance(itemStack)) {return true;}
        }
        return false;
    }

    /**
     * Checks if the provided Player can afford the costs of mining a block.
     * @param player The Player who attempted to mine the block
     * @param apply Whether to immediately apply all mining costs and rewards to the provided player, in
     *                         case they can afford the costs.
     * @return the first cost detected that the Player can't afford, or null if they can afford everything.
     */
    @Nullable
    public Reward applyCostsAndRewards(Player player, boolean apply) {
        Lang lang = RiseResourcesPlugin.getInstance().getLang();
        for (Reward cost : this.costs) {
            if (!cost.canAfford(player)) {
                lang.sendCannotAffordMessage(player, cost);
                return cost;
            }
        }
        if (apply) {
            for (Reward cost : this.costs) {
                cost.apply(player);
                lang.sendDeductedMessage(player, cost);
            }
            for (Reward reward : this.rewards) {
                reward.apply(player);
                lang.sendRewardedMessage(player, reward);
            }
        }
        return null;
    }

    public void executeCommands(Player player) {
        this.commands.forEach((c) -> {
            c.execute(player);
        });
    }

    @Nullable
    public BlockType chance() {
        if (this.chance.isEmpty()) {return null;}
        return this.chance.ceilingEntry(Math.random()*this.totalWeight).getValue();
    }

    public Set<BlockType> getMaterials() {
        return Collections.unmodifiableSet(this.materials);
    }

    public BlockType getBreakMaterial() {
        return this.breakMaterial;
    }

    public Long getRegenerationDelay() {
        return this.regenerationDelay;
    }

    public Collection<ToolType> getTools() {
        return Collections.unmodifiableCollection(this.tools);
    }

    @Nullable
    public String getToolMessage() {
        return this.toolMessage;
    }

    public int getToolDamage() {
        return this.toolDamage;
    }

    public Map<Double,BlockType> getChanceMap() {
        return Collections.unmodifiableMap(this.chance);
    }

    public List<DataEntry.Command> getCommands() {
        return this.commands;
    }

    public boolean cancelDrop() {
    	return cancelDrop;
    }
    
    public boolean isAgeable() {
        return age != null;
    }

    public int getAge() {
        return age;
    }

    public static class Command {
        protected final Integer delay;
        protected final DataEntry.Command.As as;
        protected final String cmd;

        public Command(Map<String, Object> map) {
            this.delay = (Integer) map.getOrDefault("delay", 0);
            this.as = DataEntry.Command.As.valueOf((String) map.getOrDefault("as", "PLAYER"));
            this.cmd = (String) map.getOrDefault("cmd", (Object) null);
        }

        public void execute(Player player) {
            Bukkit.getScheduler().runTaskLater(RiseResourcesPlugin.getInstance(), () -> {
                Object sender;
                if (this.as == DataEntry.Command.As.PLAYER) {
                    sender = player;
                } else {
                    sender = Bukkit.getConsoleSender();
                }

                String cmd = this.cmd;
                cmd = StringUtils.replace(cmd, "{player}", player.getName());
                Bukkit.dispatchCommand((CommandSender) sender, cmd);
            }, (long) this.delay * 20L);
        }

        public enum As {
            CONSOLE,
            OP,
            PLAYER
        }
    }
}