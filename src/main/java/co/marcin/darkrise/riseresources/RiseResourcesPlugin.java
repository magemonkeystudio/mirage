package co.marcin.darkrise.riseresources;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiseResourcesPlugin extends JavaPlugin {
    private static RiseResourcesPlugin instance;

    public static RiseResourcesPlugin getInstance() {
        return instance;
    }

    private final Data data = new Data();

    public Data getData() {
        return this.data;
    }


    private final List<String> disabledRegions = new ArrayList<>();

    public List<String> getDisabledRegions() {
        return this.disabledRegions;
    }

    private boolean isWorldGuardEnabled;
    private final List<String> disabledWorlds = new ArrayList<>();
    private boolean isTownyHookEnabled;

    public List<String> getDisabledWorlds() {
        return this.disabledWorlds;
    }

    public boolean isWorldGuardEnabled() {
        return this.isWorldGuardEnabled;
    }

    public boolean isTownyHookEnabled() {
        return this.isTownyHookEnabled;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!(new File(getDataFolder(), "config.yml")).exists()) {
            getLogger().info("Saving default config.");
            saveDefaultConfig();
        }

        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new CropListener(), this);
        getData().load(getConfig());

        getData().setStorageFile(new File(getDataFolder(), "regen.yml"));

        this.isWorldGuardEnabled = (getServer().getPluginManager().getPlugin("WorldGuard") != null);
        this.isTownyHookEnabled = (getConfig().getBoolean("towny") && getServer().getPluginManager().getPlugin("Towny") != null);
        try {
            getData().loadRegenerationEntries();
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (getConfig().contains("disabled-regions")) {
            this.disabledRegions.addAll(getConfig().getStringList("disabled-regions"));
        }


        if (getConfig().contains("disabled-worlds")) {
            this.disabledWorlds.addAll(getConfig().getStringList("disabled-worlds"));
        }


        getData().startRegenerationWatchdog();

        CommandExecutor cmd = new CommandExec();
        getCommand("resources").setExecutor(cmd);
        getCommand("resources").setTabCompleter((TabCompleter) cmd);
        getLogger().info("v" + getDescription().getVersion() + " Enabled");
    }

    @Override
    public void onDisable() {
        getData().stopRegenerationWatchdog();

        try {
            getData().saveRegenerationEntries();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getLogger().info("v" + getDescription().getVersion() + " Disabled");
    }

    public void reloadConfigs() {
        reloadConfig();
        getData().load(getConfig());
        if (getConfig().contains("disabled-regions")) {
            this.disabledRegions.addAll(getConfig().getStringList("disabled-regions"));
        }


        if (getConfig().contains("disabled-worlds")) {
            this.disabledWorlds.addAll(getConfig().getStringList("disabled-worlds"));
        }
    }
}