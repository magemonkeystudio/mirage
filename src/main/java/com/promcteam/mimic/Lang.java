package com.promcteam.mimic;

import com.promcteam.mimic.rewards.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;

public class Lang {
    private final HashMap<Class<? extends Reward>, String> cannotAfford = new HashMap<>();
    private final HashMap<Class<? extends Reward>, String> deducted     = new HashMap<>();
    private final HashMap<Class<? extends Reward>, String> rewarded     = new HashMap<>();

    public Lang(Mimic plugin) {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        loadRewardClass(config, VanillaExpReward.class, VanillaExpReward.NAME);
        loadRewardClass(config, VaultMoneyReward.class, VaultMoneyReward.NAME);
        loadRewardClass(config, JobsMoneyReward.class, JobsMoneyReward.NAME);
        loadRewardClass(config, JobsExpReward.class, JobsExpReward.NAME);
        loadRewardClass(config, JobsPointsReward.class, JobsPointsReward.NAME);
    }

    private void loadRewardClass(YamlConfiguration config, Class<? extends Reward> clazz, String name) {
        String value;
        value = config.getString("rewards.cannot-afford." + name);
        if (value != null) {
            this.cannotAfford.put(clazz, ChatColor.translateAlternateColorCodes('&', value));
        }
        value = config.getString("rewards.deducted." + name);
        if (value != null) {
            this.deducted.put(clazz, ChatColor.translateAlternateColorCodes('&', value));
        }
        value = config.getString("rewards.rewarded." + name);
        if (value != null) {
            this.rewarded.put(clazz, ChatColor.translateAlternateColorCodes('&', value));
        }
    }

    public void sendCannotAffordMessage(Player player, AmountReward reward) {
        String message = this.cannotAfford.get(reward.getClass());
        if (message != null && !message.equals("")) {
            message = message.replace("{amount}", String.valueOf(-reward.getAmount()));
            String[] args = reward.getMessageArgs();
            for (int i = 0; i < args.length; i++) {
                message = message.replace('{' + String.valueOf(i) + '}', args[0]);
            }
            player.sendMessage(message);
        }
    }

    public void sendDeductedMessage(Player player, AmountReward reward) {
        String message = this.deducted.get(reward.getClass());
        if (message != null && !message.equals("")) {
            message = message.replace("{amount}", String.valueOf(-reward.getAmount()));
            String[] args = reward.getMessageArgs();
            for (int i = 0; i < args.length; i++) {
                message = message.replace('{' + String.valueOf(i) + '}', args[0]);
            }
            player.sendMessage(message);
        }
    }

    public void sendRewardedMessage(Player player, AmountReward reward) {
        String message = this.rewarded.get(reward.getClass());
        if (message != null && !message.equals("")) {
            message = message.replace("{amount}", String.valueOf(reward.getAmount()));
            String[] args = reward.getMessageArgs();
            for (int i = 0; i < args.length; i++) {
                message = message.replace('{' + String.valueOf(i) + '}', args[0]);
            }
            player.sendMessage(message);
        }
    }
}
