package com.promcteam.mirage.requirements;

import com.promcteam.fusion.CraftingTable;
import com.promcteam.fusion.LevelFunction;
import com.promcteam.fusion.cfg.Cfg;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RPGCraftingProfessionRequirement extends Requirement {
    public static final String NAME = "RPGCRAFTING_profession";

    private final CraftingTable profession;
    private final int           level;

    public RPGCraftingProfessionRequirement(String fullString) {
        super(fullString);

        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        CraftingTable profession = Cfg.getTable(split[1]);
        if (profession == null) throw new IllegalArgumentException("Unknown profession \"" + split[1] + '\"');
        this.profession = profession;
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meets(@NotNull Player player) {
        return LevelFunction.getLevel(player, profession) >= this.level;
    }
}
