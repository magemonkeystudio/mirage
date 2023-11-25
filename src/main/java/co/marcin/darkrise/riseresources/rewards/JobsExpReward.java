package co.marcin.darkrise.riseresources.rewards;

import co.marcin.darkrise.riseresources.RiseResourcesPlugin;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JobsExpReward extends AmountReward {
    public static final String NAME = "JOBS_exp";

    private final Job job;

    public JobsExpReward(String fullString) {
        super(removeJob(fullString));
        String jobId = fullString.split(":")[0].substring((JobsExpReward.NAME+'_').length());
        this.job = Jobs.getJob(jobId);
        if (this.job == null) {
            throw new IllegalArgumentException("Unknown job \""+jobId+"\"");
        }
    }

    private static String removeJob(String fullString) {
        String[] split = fullString.split(":");
        if (split.length != 2) {return fullString;} // Purposefully cause exception in super constructor
        if (!split[0].startsWith(JobsExpReward.NAME+'_') || split[0].length() <= (JobsExpReward.NAME+'_').length()) {
            throw new IllegalArgumentException("A job name must be specified, like \"JOBS_exp_miner:5\"");
        }
        return JobsExpReward.NAME+':'+split[1];
    }

    @Override
    @NotNull
    public String getName() {return NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        JobProgression jobProgression = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression(this.job);
        if (jobProgression == null) {return false;}
        return jobProgression.getExperience() >= -this.amount;
    }

    @Override
    public void apply(@NotNull Player player) {
        JobProgression job = Jobs.getPlayerManager().getJobsPlayer(player).getJobProgression(this.job);
        if (job == null) {
            RiseResourcesPlugin.getInstance().debug("Failed to execute reward \""+JobsExpReward.NAME+'_'+this.job.getName()+':'+this.amount+"\": Player \""+player.getName()+"\" does not belong to this job");
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
