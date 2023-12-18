package co.marcin.darkrise.riseresources.requirements;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class EnchantmentRequirement extends Requirement {
    public static final String NAME = "enchantment";

    private final Enchantment enchantment;
    private final int         level;

    public EnchantmentRequirement(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        this.enchantment = Enchantment.getByKey(NamespacedKey.fromString(split[1]
                .replace(' ', '_')
                .replace('-', '_')
                .toLowerCase()));
        if (this.enchantment == null) throw new IllegalArgumentException("Unknown enchantment \""+split[1]+'\"');
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meets(@NotNull Player player) {
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        if (meta == null) return false;
        return meta.getEnchantLevel(this.enchantment) >= this.level;
    }
}
