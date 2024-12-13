package studio.magemonkey.mirage.requirements;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.fusion.CraftingTable;
import studio.magemonkey.fusion.LevelFunction;
import studio.magemonkey.fusion.cfg.Cfg;

public class FusionProfessionRequirement extends Requirement {
    public static final String NAME = "FUSION_profession";

    private final CraftingTable profession;
    private final int           level;

    public FusionProfessionRequirement(String fullString) {
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
