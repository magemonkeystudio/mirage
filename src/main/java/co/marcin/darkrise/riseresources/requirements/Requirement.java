package co.marcin.darkrise.riseresources.requirements;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public abstract class Requirement {
    private final String fullString;

    public static Requirement make(String string) {
        if (string.startsWith(SkillAPISkillRequirement.NAME)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ProSkillAPI")) {
                throw new IllegalStateException("ProSkillAPI is not enabled");
            }
            return new SkillAPISkillRequirement(string);
        } else if (string.startsWith(SkillAPIClassRequirement.NAME)) {
            if (!Bukkit.getPluginManager().isPluginEnabled("ProSkillAPI")) {
                throw new IllegalStateException("ProSkillAPI is not enabled");
            }
            return new SkillAPIClassRequirement(string);
        } else {
            throw new IllegalArgumentException("Unknown name");
        }
    }

    public Requirement(String fullString) {
        if (!fullString.startsWith(this.getName())) {throw new IllegalArgumentException();}
        this.fullString = fullString;
    }

    @NotNull
    public abstract String getName();

    public abstract boolean meets(@NotNull Player player);

    @Override
    public String toString() {return fullString;}
}
