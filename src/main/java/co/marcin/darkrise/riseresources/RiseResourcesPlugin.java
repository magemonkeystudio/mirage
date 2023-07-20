package co.marcin.darkrise.riseresources;

import me.angeschossen.lands.api.integration.LandsIntegration;
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
    private       Lang lang;

    public Data getData() {return this.data;}

    private final List<String> disabledRegions = new ArrayList<>();

    public List<String> getDisabledRegions() {
        return this.disabledRegions;
    }

    public boolean areRegionsBlacklisted(){
        return regionsBlacklisted;
    }

    private boolean regionsBlacklisted;
    private boolean isWorldGuardEnabled;
    private final List<String> disabledWorlds = new ArrayList<>();
    private boolean worldsBlacklisted;
    private boolean isTownyHookEnabled;
    private boolean isFactionsUUIDEnabled;
    private LandsIntegration landsIntegration;

    private boolean debug = false;

    public void debug(String message) {
        if (debug) {this.getLogger().info(message);}
    }

    public List<String> getDisabledWorlds() {
        return this.disabledWorlds;
    }

    public boolean areWorldsBlacklisted(){
        return worldsBlacklisted;
    }

    public boolean isWorldGuardEnabled() {
        return this.isWorldGuardEnabled;
    }

    public boolean isTownyHookEnabled() {
        return this.isTownyHookEnabled;
    }

    public boolean isFactionsUUIDEnabled(){
        return this.isFactionsUUIDEnabled;
    }

    public LandsIntegration getLandsIntegration(){
        return landsIntegration;
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
        this.isFactionsUUIDEnabled = (getConfig().getBoolean("factionsuuid") && getServer().getPluginManager().getPlugin("Factions") != null);

        if(getConfig().getBoolean("lands") && getServer().getPluginManager().getPlugin("Lands") != null){
            this.landsIntegration = new LandsIntegration(this);
        }else{
            this.landsIntegration = null;
        }
        try {
            getData().loadRegenerationEntries();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.debug = getConfig().getBoolean("debug", false);

        regionsBlacklisted = true;
        if (getConfig().contains("disabled-regions.list")) {
            this.disabledRegions.addAll(getConfig().getStringList("disabled-regions.list"));
            this.regionsBlacklisted = !getConfig().getBoolean("disabled-regions.whitelist", false);
        }

        worldsBlacklisted = true;
        if (getConfig().contains("disabled-worlds.list")) {
            this.disabledWorlds.addAll(getConfig().getStringList("disabled-worlds.list"));
            this.worldsBlacklisted = !getConfig().getBoolean("disabled-worlds.whitelist", false);
        }

        this.lang = new Lang(this);


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

        this.debug = getConfig().getBoolean("debug", false);

        disabledRegions.clear();
        regionsBlacklisted = true;
        if (getConfig().contains("disabled-regions.list")) {

            this.disabledRegions.addAll(getConfig().getStringList("disabled-regions.list"));
            this.regionsBlacklisted = !getConfig().getBoolean("disabled-regions.whitelist", false);
        }

        disabledWorlds.clear();
        worldsBlacklisted = true;
        if (getConfig().contains("disabled-worlds.list")) {
            this.disabledWorlds.addAll(getConfig().getStringList("disabled-worlds.list"));
            this.worldsBlacklisted = !getConfig().getBoolean("disabled-worlds.whitelist", false);

        }

        this.lang = new Lang(this);
    }

    public Lang getLang() {return lang;}
}