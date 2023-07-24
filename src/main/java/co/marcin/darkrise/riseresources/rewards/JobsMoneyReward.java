package co.marcin.darkrise.riseresources.rewards;

import com.gamingmesh.jobs.Jobs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JobsMoneyReward extends Reward {
    public static final String NAME = "JOBS_money";

    public JobsMoneyReward(String fullString) {super(fullString);}

    @Override
    @NotNull
    public String getName() {return JobsMoneyReward.NAME;}

    @Override
    public boolean canAfford(@NotNull Player player) {
        return Jobs.getEconomy().getEconomy().hasMoney(player, -this.amount);
    }

    @Override
    public void apply(@NotNull Player player) {
        if (this.amount >= 0) {
            Jobs.getEconomy().getEconomy().depositPlayer(player, this.amount);
        } else {
            Jobs.getEconomy().getEconomy().withdrawPlayer(player, -this.amount);
        }
    }
}
