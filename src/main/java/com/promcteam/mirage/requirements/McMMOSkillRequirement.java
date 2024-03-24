package com.promcteam.mirage.requirements;

import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class McMMOSkillRequirement extends Requirement {
    public static final String NAME = "MCMMO_skill";

    private final PrimarySkillType skill;
    private final int              level;

    public McMMOSkillRequirement(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        try {
            this.skill = PrimarySkillType.valueOf(split[1].replace(' ', '_').replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown skill \"" + split[1] + '\"');
        }
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meets(@NotNull Player player) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) return false;
        return mcMMOPlayer.getSkillLevel(this.skill) >= this.level;
    }
}
