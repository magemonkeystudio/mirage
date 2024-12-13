package studio.magemonkey.mirage.rewards;

import com.gmail.nossr50.datatypes.experience.XPGainReason;
import com.gmail.nossr50.datatypes.experience.XPGainSource;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.mirage.Mirage;

public class McMMOExperienceReward extends AmountReward {
    public static final String NAME = "MCMMO_exp";

    private final PrimarySkillType skill;

    public McMMOExperienceReward(String fullString) {
        super(fullString, parseAmount(fullString));
        String[] split = fullString.split(":");
        try {
            this.skill = PrimarySkillType.valueOf(split[1].replace(' ', '_').replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown skill \"" + split[1] + '\"');
        }
    }

    private static String parseAmount(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 3) return fullString;
        return split[2];
    }

    @Override
    public boolean canAfford(@NotNull Player player) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) return false;

        return mcMMOPlayer.getSkillXpLevelRaw(this.skill) >= -this.amount;
        // mcMMO does not support lowering levels, so only xp from current level can be used
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public void apply(@NotNull Player player) {
        McMMOPlayer mcMMOPlayer = UserManager.getPlayer(player);
        if (mcMMOPlayer == null) {
            Mirage.getInstance()
                    .debug("Failed to execute reward \"" + NAME + ':' + this.skill.name() + ':' + this.amount
                            + "\": Player \"" + player.getName() + "\" does not have mcMMO data");
            return;
        }
        mcMMOPlayer.applyXpGain(this.skill, (float) this.amount, XPGainReason.UNKNOWN, XPGainSource.CUSTOM);
    }
}
