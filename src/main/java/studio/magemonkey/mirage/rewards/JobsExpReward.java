package studio.magemonkey.mirage.rewards;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.mirage.Mirage;

public class JobsExpReward extends AmountReward {
    public static final String NAME = "JOBS_exp";

    private final Job job;

    public JobsExpReward(String fullString) {
        super(fullString, parseAmount(fullString));
        String jobId = fullString.split(":")[1];
        this.job = Jobs.getJob(jobId);
        if (this.job == null) {
            throw new IllegalArgumentException("Unknown job \"" + jobId + "\"");
        }
    }

    private static String parseAmount(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 3) return fullString;
        return split[2];
    }

    @Override
    @NotNull
    public String getName() {return NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        JobProgression jobProgression = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression(this.job);
        if (jobProgression == null) {
            return false;
        }
        return jobProgression.getExperience() >= -this.amount;
    }

    @Override
    public void apply(@NotNull Player player) {
        JobProgression job = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression(this.job);
        if (job == null) {
            Mirage.getInstance()
                    .debug("Failed to execute reward \"" + JobsExpReward.NAME + ':' + this.job.getName() + ':'
                            + this.amount + "\": Player \"" + player.getName() + "\" does not belong to this job");
            return;
        }
        if (this.amount >= 0) {
            job.addExperience(this.amount);
        } else {
            job.takeExperience(-this.amount);
        }
    }

    @Override
    public String[] getMessageArgs() {return new String[]{this.job.getName()};}
}
