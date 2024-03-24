package com.promcteam.mirage.rewards;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VanillaExpReward extends AmountReward {

    public static String NAME = "exp";

    public VanillaExpReward(String fullString) {
        super(fullString, parseAmount(fullString));
    }

    private static String parseAmount(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 2) return fullString;
        return split[1];
    }

    @Override
    @NotNull
    public String getName() {return NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        return this.getTotalExp(player) >= -this.amount;
    }

    @Override
    public void apply(@NotNull Player player) {
        if (this.amount >= 0) {
            player.giveExp((int) this.amount);
        } else {
            int totalExp   = this.getTotalExp(player);
            int totalScore = player.getTotalExperience();
            player.setLevel(0);
            player.setExp(0);
            player.giveExp(totalExp + (int) this.amount);
            player.setTotalExperience(totalScore);
        }
    }

    private int getTotalExp(Player player) {
        int level = player.getLevel();
        int currentAmount;
        if (level < 17) {
            currentAmount = level * level + 6 * level;
        } else if (level < 32) {
            currentAmount = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            currentAmount = (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        float exp = player.getExp();
        if (level < 16) {
            currentAmount += exp * (2 * level + 7);
        } else if (level < 31) {
            currentAmount += exp * (5 * level - 38);
        } else {
            currentAmount += exp * (9 * level - 158);
        }
        return currentAmount;
    }
}
