package com.promcteam.mimic.rewards;

import com.gamingmesh.jobs.Jobs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobsMoneyReward extends AmountReward {
    public static final String NAME = "JOBS_money";

    public JobsMoneyReward(String fullString) {
        super(fullString, parseAmount(fullString));
    }

    private static String parseAmount(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 2) return fullString;
        return split[1];
    }

    @Override
    @NotNull
    public String getName() {return JobsMoneyReward.NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        return Jobs.getEconomy().getEconomy().hasMoney(player, -this.amount);
    }

    @Override
    public void apply(@NotNull Player player) {
        if (this.amount >= 0) {
            Jobs.getEconomy().getEconomy().depositPlayer(player, this.amount);
        } else {
            Jobs.getEconomy().getEconomy().withdrawPlayer(player, -this.amount);
        }
    }
}
