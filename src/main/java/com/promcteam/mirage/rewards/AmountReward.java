package com.promcteam.mirage.rewards;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class AmountReward extends Reward {
    protected final double amount; // Can be negative for costs

    public final double getAmount() {return this.amount;}

    public AmountReward(String fullString, String amount) {
        super(fullString);
        try {
            this.amount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(amount + " is not a valid amount");
        }
    }

    public abstract boolean canAfford(@NotNull Player player);
}
