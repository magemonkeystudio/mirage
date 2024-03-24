package com.promcteam.mirage.requirements;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobsJobRequirement extends Requirement {
    public static final String NAME = "JOBS_job";

    private final Job job;
    private final int level;

    public JobsJobRequirement(String fullString) {
        super(fullString);
        String[] split = fullString.split(":");
        if (split.length == 2) {
            this.level = 1;
        } else if (split.length == 3) {
            this.level = Math.max(1, Integer.parseInt(split[2]));
        } else throw new IllegalArgumentException();
        this.job = Jobs.getJob(split[1]);
        if (this.job == null) throw new IllegalArgumentException("Unknown job \"" + split[1] + '\"');
    }

    @Override
    @NotNull
    public String getName() {
        return NAME;
    }

    @Override
    public boolean meets(@NotNull Player player) {
        JobProgression job = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression(this.job);
        return job != null && job.getLevel() >= this.level;
    }
}
