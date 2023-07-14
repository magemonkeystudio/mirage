package co.marcin.darkrise.riseresources.tools;

import com.gotofinal.darkrise.economy.DarkRiseEconomy;
import me.travja.darkrise.core.item.DarkRiseItem;
import org.bukkit.inventory.ItemStack;

public class ProMCUtilitiesToolType extends ToolType {
    public static final String PREFIX = "PROMCU_";

    ProMCUtilitiesToolType(String fullId) {super(fullId);}

    @Override
    public String getPrefix() {return PREFIX;}

    @Override
    public boolean isInstance(ItemStack itemStack) {
        DarkRiseItem riseItem = DarkRiseEconomy.getItemsRegistry().getItemByStack(itemStack);
        return riseItem != null && riseItem.getId().equals(this.id);
    }
}
