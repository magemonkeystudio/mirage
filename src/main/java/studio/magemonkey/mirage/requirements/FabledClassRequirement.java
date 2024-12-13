package studio.magemonkey.mirage.requirements;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.fabled.Fabled;
import studio.magemonkey.fabled.api.classes.FabledClass;
import studio.magemonkey.fabled.api.player.PlayerClass;

public class FabledClassRequirement extends Requirement {
    public static final String NAME = "FABLED_class";

    private final String clazz;
    private final int    level;

    public FabledClassRequirement(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        FabledClass clazz = Fabled.getClass(split[1]);
        if (clazz == null) throw new IllegalArgumentException("Unknown class \"" + split[1] + '\"');
        this.clazz = clazz.getName();
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meets(@NotNull Player player) {
        for (PlayerClass playerClass : Fabled.getData(player).getClasses()) {
            if (playerClass.getData().getName().equalsIgnoreCase(this.clazz) && playerClass.getLevel() >= this.level) {
                return true;
            }
        }
        return false;
    }
}
