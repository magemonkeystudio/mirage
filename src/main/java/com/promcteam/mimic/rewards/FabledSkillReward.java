package com.promcteam.mimic.rewards;

import com.promcteam.fabled.Fabled;
import com.promcteam.fabled.api.skills.Skill;
import com.promcteam.fabled.api.skills.SkillShot;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FabledSkillReward extends Reward {
    public static final String NAME = "FABLED_skill";

    private final SkillShot skill;
    private final int       level;

    public FabledSkillReward(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        Skill skill = Fabled.getSkill(split[1]);
        if (!(skill instanceof SkillShot))
            throw new IllegalArgumentException("\"" + split[1] + "\" is not a skillshot");
        this.skill = (SkillShot) skill;
    }

    @Override
    @NotNull
    public String getName() {return NAME;}

    @Override
    public void apply(@NotNull Player player) {
        this.skill.cast(player, this.level, true);
    }
}
