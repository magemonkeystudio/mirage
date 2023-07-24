package co.marcin.darkrise.riseresources.rewards;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a reward of a certain resource, or a cost if its amount is negative,
 * applied when mining a block.
 */
public abstract class Reward {

    public static Reward make(String string) {
        if (string.startsWith(VanillaExpReward.NAME)) {
            return new VanillaExpReward(string);
        } else if (string.startsWith(VaultMoneyReward.NAME)) {
            return new VaultMoneyReward(string);
        } else if (string.startsWith(JobsMoneyReward.NAME)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("Jobs")) {
                throw new IllegalStateException("Jobs is not enabled");
            }
            return new JobsMoneyReward(string);
        } else if (string.startsWith(JobsExpReward.NAME)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("Jobs")) {
                throw new IllegalStateException("Jobs is not enabled");
            }
            return new JobsExpReward(string);
        } else if (string.startsWith(JobsPointsReward.NAME)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("Jobs")) {
                throw new IllegalStateException("Jobs is not enabled");
            }
            return new JobsPointsReward(string);
        } else {
            throw new IllegalArgumentException("Unknown name");
        }
    }

    protected final double amount; // Can be negative for costs

    public Reward(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 2 || !split[0].equals(this.getName())) {throw new IllegalArgumentException();}
        try {
            this.amount = Double.parseDouble(split[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(split[1]+" is not a valid amount");
        }
    }

    @NotNull
    public abstract String getName();

    public final double getAmount() {return this.amount;}

    public abstract boolean canAfford(@NotNull Player player);

    public abstract void apply(@NotNull Player player);

    public String[] getMessageArgs() {return new String[0];}
}
