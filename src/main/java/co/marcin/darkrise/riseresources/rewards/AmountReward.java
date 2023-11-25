package co.marcin.darkrise.riseresources.rewards;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AmountReward extends Reward {
    protected final double amount; // Can be negative for costs

    public final double getAmount() {return this.amount;}

    public AmountReward(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length != 2) {throw new IllegalArgumentException();}
        try {
            this.amount = Double.parseDouble(split[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(split[1]+" is not a valid amount");
        }
    }

    public abstract boolean canAfford(@NotNull Player player);
}
