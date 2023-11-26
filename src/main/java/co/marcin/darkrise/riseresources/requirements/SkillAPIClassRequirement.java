package co.marcin.darkrise.riseresources.requirements;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.player.PlayerClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SkillAPIClassRequirement extends Requirement {
    public static final String NAME = "SKILLAPI_class";

    private final String clazz;
    private final int    level;

    public SkillAPIClassRequirement(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        RPGClass clazz = SkillAPI.getClass(split[1]);
        if (clazz == null) throw new IllegalArgumentException("Unknown class \""+split[1]+'\"');
        this.clazz = clazz.getName();
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meets(@NotNull Player player) {
        for (PlayerClass playerClass : SkillAPI.getPlayerData(player).getClasses()) {
            if (playerClass.getData().getName().equalsIgnoreCase(this.clazz) && playerClass.getLevel() >= this.level) {
                return true;
            }
        }
        return false;
    }
}
