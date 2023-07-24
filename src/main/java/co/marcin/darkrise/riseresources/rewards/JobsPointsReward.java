package co.marcin.darkrise.riseresources.rewards;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.PlayerPoints;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobsPointsReward extends Reward {
    public static final String NAME = "JOBS_points";

    public JobsPointsReward(String fullString) {super(fullString);}

    @Override
    @NotNull
    public String getName() {return JobsPointsReward.NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        return Jobs.getPlayerManager().getJobsPlayer(player).getPointsData().havePoints(-this.amount);
    }

    @Override
    public void apply(@NotNull Player player) {
        PlayerPoints playerPoints = Jobs.getPlayerManager().getJobsPlayer(player).getPointsData();
        if (this.amount >= 0) {
            playerPoints.addPoints(this.amount);
        } else {
            playerPoints.takePoints(-this.amount);
        }
    }
}
