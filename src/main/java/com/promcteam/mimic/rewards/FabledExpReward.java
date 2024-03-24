package com.promcteam.mimic.rewards;

import com.promcteam.fabled.Fabled;
import com.promcteam.fabled.api.enums.ExpSource;
import com.promcteam.fabled.api.player.PlayerClass;
import com.promcteam.fabled.api.player.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FabledExpReward extends AmountReward {
    public static final String NAME = "FABLED_exp";

    private final String group;

    public FabledExpReward(String fullString) {
        super(fullString, parseAmount(fullString));
        String group = fullString.split(":")[1].toLowerCase();
        if (!Fabled.getGroups().contains(group))
            throw new IllegalArgumentException("Unknown class group \"" + group + "\"");
        this.group = group;
    }

    private static String parseAmount(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 3) return fullString;
        return split[2];
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public void apply(@NotNull Player player) {
        PlayerData  playerData = Fabled.getPlayerData(player);
        PlayerClass clazz      = playerData.getClass(this.group);
        if (this.amount >= 0) {
            clazz.giveExp(this.amount, ExpSource.BLOCK_BREAK, true);
        } else {
            clazz.loseExp(-this.amount, false, true);
        }
    }

    @Override
    public boolean canAfford(@NotNull Player player) {
        PlayerData  playerData = Fabled.getPlayerData(player);
        PlayerClass clazz      = playerData.getClass(this.group);
        if (clazz == null) return false;
        return clazz.getTotalExp() >= -this.amount;
    }
}
