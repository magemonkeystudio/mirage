package co.marcin.darkrise.riseresources.rewards;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillShot;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkillAPISkillReward extends Reward {
    public static final String NAME = "SKILLAPI_skill";

    private final SkillShot skill;
    private final int       level;

    public SkillAPISkillReward(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        Skill skill = SkillAPI.getSkill(split[1]);
        if (!(skill instanceof SkillShot)) throw new IllegalArgumentException("\""+split[1]+"\" is not a skillshot");
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
