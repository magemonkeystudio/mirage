package co.marcin.darkrise.riseresources;

import com.google.common.collect.Iterators;
import com.gotofinal.darkrise.economy.DarkRiseEconomy;
import me.travja.darkrise.core.Debugger;
import me.travja.darkrise.core.item.DarkRiseItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DataEntry {
    private final ItemStack material;
    private final ItemStack breakMaterial;
    private final Long regenerationDelay;
    private final Collection<DarkRiseItem> tools = new HashSet();
    private final Map<ItemStack, Double> chance = new HashMap();
    private final List<DataEntry.Command> commands = new ArrayList();
    private String toolMessage;
    private Integer toolDamage;
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
                if (tools.get("allowed") instanceof String) {
                    String toolString = (String) tools.get("allowed");
                    DarkRiseItem item;
                    if(toolString.startsWith("VANILLA_")){
                        Material material = Material.getMaterial(toolString.substring("VANILLA_".length()));
                        Validate.notNull(material, "Invalid vanilla material: " + toolString);
                        item = DarkRiseEconomy.getItemsRegistry().getVanillaItemByStack(new ItemStack(material));
                    }else {
                        item = DarkRiseEconomy.getItemsRegistry().getItemById(toolString);
                    }
                    Validate.notNull(item, "Invalid item: " + toolString);
                    this.tools.add(item);
                } else {
                    if (!(tools.get("allowed") instanceof List)) {
                        throw new IllegalArgumentException("Invalid data type.");
                    }

                    Iterator var3 = ((List) tools.get("allowed")).iterator();

                    while (var3.hasNext()) {
                        String toolString = (String) var3.next();
                        DarkRiseItem item;
                        if(toolString.startsWith("VANILLA_")){
                            Material material = Material.getMaterial(toolString.substring("VANILLA_".length()));
                            Validate.notNull(material, "Invalid vanilla material: " + toolString);
                            item = DarkRiseEconomy.getItemsRegistry().getVanillaItemByStack(new ItemStack(material));
                        }else {
                            item = DarkRiseEconomy.getItemsRegistry().getItemById(toolString);
                        }
                        Validate.notNull(item, "Invalid item: " + toolString);
                        this.tools.add(item);
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
            Debugger.log("Added chance \"" + e.getKey().toUpperCase() + "\"");
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
        if (this.getTools().isEmpty()) {
            return true;
        } else {
            DarkRiseItem riseItem = DarkRiseEconomy.getItemsRegistry().getItemByStack(itemStack);
            return riseItem != null && this.getTools().stream().anyMatch((item) -> item.equals(riseItem));
        }
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

    public Collection<DarkRiseItem> getTools() {
        return this.tools;
    }

    public String getToolMessage() {
        return this.toolMessage;
    }

    public Integer getToolDamage() {
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