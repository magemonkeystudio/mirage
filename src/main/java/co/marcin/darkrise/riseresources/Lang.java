package co.marcin.darkrise.riseresources;

import co.marcin.darkrise.riseresources.rewards.Reward;
import co.marcin.darkrise.riseresources.rewards.VanillaExpReward;
import co.marcin.darkrise.riseresources.rewards.VaultMoneyReward;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

public class Lang {
    private final HashMap<Class<? extends Reward>,String> cannotAfford = new HashMap<>();
    private final HashMap<Class<? extends Reward>,String> deducted     = new HashMap<>();
    private final HashMap<Class<? extends Reward>,String> rewarded     = new HashMap<>();

    public Lang(RiseResourcesPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {plugin.saveResource("lang.yml", false);}
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        loadRewardClass(config, VanillaExpReward.class, VanillaExpReward.NAME);
        loadRewardClass(config, VaultMoneyReward.class, VaultMoneyReward.NAME);
    }

    private void loadRewardClass(YamlConfiguration config, Class<? extends Reward> clazz, String name) {
        String value;
        value = config.getString("rewards.cannot-afford."+name);
        if (value != null) {this.cannotAfford.put(clazz, ChatColor.translateAlternateColorCodes('&', value));}
        value = config.getString("rewards.deducted."+name);
        if (value != null) {this.deducted.put(clazz, ChatColor.translateAlternateColorCodes('&', value));}
        value = config.getString("rewards.rewarded."+name);
        if (value != null) {this.rewarded.put(clazz, ChatColor.translateAlternateColorCodes('&', value));}
    }

    public void sendCannotAffordMessage(Player player, Reward reward) {
        String message = this.cannotAfford.get(reward.getClass());
        if (message != null && !message.equals("")) {
            player.sendMessage(message
                    .replace("{amount}", String.valueOf(reward.getAmount()))
                    .replace("{current}", String.valueOf(reward.getCurrentAmount(player))));
        }
    }

    public void sendDeductedMessage(Player player, Reward reward) {
        String message = this.deducted.get(reward.getClass());
        if (message != null && !message.equals("")) {
            player.sendMessage(message
                    .replace("{amount}", String.valueOf(reward.getAmount()))
                    .replace("{current}", String.valueOf(reward.getCurrentAmount(player))));
        }
    }

    public void sendRewardedMessage(Player player, Reward reward) {
        String message = this.rewarded.get(reward.getClass());
        if (message != null && !message.equals("")) {
            player.sendMessage(message
                    .replace("{amount}", String.valueOf(reward.getAmount()))
                    .replace("{current}", String.valueOf(reward.getCurrentAmount(player))));
        }
    }
}
