package co.marcin.darkrise.riseresources;

import com.google.common.collect.Iterators;
import com.gotofinal.darkrise.economy.DarkRiseEconomy;
import dev.lone.itemsadder.api.CustomStack;
import io.th0rgal.oraxen.api.OraxenItems;
import mc.promcteam.engine.modules.IModule;
import me.travja.darkrise.core.item.DarkRiseItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.modules.ModuleItem;
import su.nightexpress.quantumrpg.modules.api.QModuleDrop;
import su.nightexpress.quantumrpg.stats.items.ItemStats;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DataEntry {
    private static final String PROMCUTILITIES_KEY = "PROMCU_";
    private static final String ORAXEN_KEY = "ORAXEN_";
    private static final String ITEMSADDER_KEY = "ITEMSADDER_";
    private static final String PRORPGITEMS_KEY = "PRORPGITEMS_";
    
    private final ItemStack material;
    private final ItemStack breakMaterial;
    private final Long regenerationDelay;
    private final Set<String> tools = new HashSet<>();
    private final Map<ItemStack, Double> chance = new HashMap();
    private final List<DataEntry.Command> commands = new ArrayList();
    private String toolMessage;
    private int toolDamage = 0;
    private Integer age;
    private boolean cancelDrop;
    
    public DataEntry(Map<String, Object> map) {
        Validate.notNull(map);
        this.material = Utils.getItemFromString((String) map.get("material"));
        Validate.notNull(this.material, "Invalid material: " + map.get("material"));
//        String matString = (String) map.get("break-material");
//        if(matString.contains(":")) {
//            breakDurability = Short.parseShort(matString.split(":")[1]);
//            matString = matString.split(":")[0];
//        }
        this.breakMaterial = Utils.getItemFromString((String) map.get("break-material"));
        Validate.notNull(this.breakMaterial, "Invalid material: " + map.get("break-material"));
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
                    if (toolString.startsWith(PROMCUTILITIES_KEY)) {
                        if (!Bukkit.getPluginManager().isPluginEnabled("ProMCUtilities")){
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+toolString+"\", ProMCUtilities is not enabled");
                            continue;
                        }
                        if (DarkRiseEconomy.getItemsRegistry().getItemById(toolString.substring(PROMCUTILITIES_KEY.length())) == null) {
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown ProMCUtilities tool \""+toolString+'"');
                            continue;
                        }
                        this.tools.add(toolString);
                    } else if (toolString.startsWith(ORAXEN_KEY)) {
                        if (!Bukkit.getPluginManager().isPluginEnabled("Oraxen")){
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+toolString+"\", Oraxen is not enabled");
                            continue;
                        }
                        if (OraxenItems.getItemById(toolString.substring(ORAXEN_KEY.length())) == null) {
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown Oraxen tool \""+toolString+'"');
                            continue;
                        }
                        this.tools.add(toolString);
                    } else if (toolString.startsWith(ITEMSADDER_KEY)) {
                        if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")){
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+toolString+"\", ItemsAdder is not enabled");
                            continue;
                        }
                        if (CustomStack.getInstance(toolString.substring(ITEMSADDER_KEY.length())) == null) {
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown ItemsAdder tool \""+toolString+'"');
                            continue;
                        }
                        this.tools.add(toolString);
                    } else if (toolString.startsWith(PRORPGITEMS_KEY)) {
                        if (!Bukkit.getPluginManager().isPluginEnabled("ProRPGItems")){
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring allowed tool \""+toolString+"\", ProRPGItems is not enabled");
                            continue;
                        }
                        String itemId = toolString.substring(PRORPGITEMS_KEY.length());
                        boolean found = false;
                        for (IModule<?> module : QuantumRPG.getInstance().getModuleManager().getModules()) {
                            if (module instanceof QModuleDrop && ((QModuleDrop<? extends ModuleItem>) module).getItemById(itemId) != null) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown ProRPGItems tool \""+toolString+'"');
                            continue;
                        }
                        this.tools.add(toolString);
                    } else {
                        try {
                            this.tools.add(Material.valueOf(toolString
                                    .trim()
                                    .toUpperCase()
                                    .replace(' ', '_')
                                    .replace('-', '_')).name());

                        } catch (IllegalArgumentException e) {
                            RiseResourcesPlugin.getInstance().getLogger().warning("Ignoring unknown material tool \""+toolString+'"');
                        }
                    }
                }
            }

            this.toolMessage = ChatColor.translateAlternateColorCodes('&', (String) tools.getOrDefault("message", (Object) null));
            this.toolDamage = (Integer) tools.getOrDefault("damage", 0);
        }

        Validate.isTrue(map.containsKey("chance"), "Config must contain 'chance' section");
        Validate.isTrue(map.get("chance") instanceof Map, "'chance' must be a section");
        this.chance.putAll(((Map<String, Object>) map.get("chance")).entrySet().stream().collect(Collectors.toMap(e -> {
            ItemStack m = Utils.getItemFromString(e.getKey().toUpperCase());
            Validate.notNull(m, "Invalid material: " + e.getKey());
            RiseResourcesPlugin.getInstance().debug("Added chance \"" + e.getKey().toUpperCase() + "\"");
            return m;
        }, e -> (Double) e.getValue())));
        if (map.containsKey("command")) {
            Validate.isTrue(map.get("command") instanceof List, "'command' section must be a list");
            List<Map<String, Object>> commandList = (List) map.get("command");
            this.commands.addAll(commandList.stream().map(Command::new).collect(Collectors.toList()));
        }

        if (map.containsKey("age")) {
            age = (int) map.get("age");
        }

        Bukkit.getLogger().info("Created DataEntry for material: " + this.material.getType().name());
    }

    public boolean isUsableTool(ItemStack itemStack) {
        if (this.getTools().isEmpty()) {return true;}
        for (String toolString : this.tools) {
            if (toolString.startsWith(PROMCUTILITIES_KEY)) {
                DarkRiseItem riseItem = DarkRiseEconomy.getItemsRegistry().getItemByStack(itemStack);
                if (riseItem != null && riseItem.getId().equals(toolString.substring(PROMCUTILITIES_KEY.length()))) {return true;}
            } else if (toolString.startsWith(ORAXEN_KEY)) {
                String itemId = OraxenItems.getIdByItem(itemStack);
                if (itemId != null && itemId.equals(toolString.substring(ORAXEN_KEY.length()))) {return true;}
            } else if (toolString.startsWith(ITEMSADDER_KEY)) {
                CustomStack customStack = CustomStack.byItemStack(itemStack);
                if (customStack != null && customStack.getId().equals(toolString.substring(ITEMSADDER_KEY.length()))) {return true;}
            } else if (toolString.startsWith(PRORPGITEMS_KEY)) {
                String itemId = ItemStats.getId(itemStack);
                if (itemId != null && itemId.equals(toolString.substring(PRORPGITEMS_KEY.length()))) {return true;}
            } else {
                if (itemStack.getType().name().equalsIgnoreCase(toolString)) {return true;}
            }
        }
        return false;
    }

    public void executeCommands(Player player) {
        this.commands.forEach((c) -> {
            c.execute(player);
        });
    }

    public ItemStack chance() {
        float random = (float) Math.random();
        float c = 0.0F;
        Iterator iter = this.chance.entrySet().iterator();

        Entry e;
        do {
            if (!iter.hasNext()) {
                return Iterators.getLast(this.chance.keySet().iterator());
            }

            e = (Entry) iter.next();
            c = (float) ((double) c + (Double) e.getValue());
        } while (c < random);

        return (ItemStack) e.getKey();
    }

    public ItemStack getMaterial() {
        return this.material;
    }

    public ItemStack getBreakMaterial() {
        return this.breakMaterial;
    }

    public Long getRegenerationDelay() {
        return this.regenerationDelay;
    }

    public Collection<String> getTools() {
        return Collections.unmodifiableCollection(this.tools);
    }

    @Nullable
    public String getToolMessage() {
        return this.toolMessage;
    }

    public int getToolDamage() {
        return this.toolDamage;
    }

    public Map<ItemStack, Double> getChance() {
        return this.chance;
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