package co.marcin.darkrise.riseresources.rewards;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultMoneyReward extends Reward {
    private static Economy economy;
    public static String NAME = "VAULT_money";

    public VaultMoneyReward(String fullString) {
        super(fullString);
        if (VaultMoneyReward.economy == null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp == null) {throw new IllegalStateException("No Vault Economy implementation found");}
                VaultMoneyReward.economy = rsp.getProvider();
                RiseResourcesPlugin.getInstance().getLogger().info("Hooked to Vault economy");
        } else {
            throw new IllegalStateException("Vault is not enabled");
        }
    }

    @Override
    @NotNull
    public String getName() {return NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        if (VaultMoneyReward.economy == null) {throw new IllegalStateException("Vault is not enabled");}
        return VaultMoneyReward.economy.getBalance(player) >= -this.amount;
    }

    @Override
    public void apply(@NotNull Player player) {
        if (VaultMoneyReward.economy == null) {throw new IllegalStateException("Vault is not enabled");}
        if (this.amount >= 0) {
            VaultMoneyReward.economy.depositPlayer(player, this.amount);
        } else {
            VaultMoneyReward.economy.withdrawPlayer(player, -this.amount);
        }
    }
}
