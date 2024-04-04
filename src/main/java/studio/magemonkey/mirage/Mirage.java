package studio.magemonkey.mirage;

import lombok.Getter;
import me.angeschossen.lands.api.integration.LandsIntegration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mirage extends JavaPlugin {
    @Getter
    private static Mirage instance;

    @Getter
    private final Data data = new Data();
    @Getter
    private       Lang lang;

    @Getter
    private final List<String> disabledRegions = new ArrayList<>();

    public boolean areRegionsBlacklisted() {
        return regionsBlacklisted;
    }

    private       boolean          regionsBlacklisted;
    @Getter
    private       boolean          worldGuardEnabled;
    @Getter
    private final List<String>     disabledWorlds = new ArrayList<>();
    private       boolean          worldsBlacklisted;
    @Getter
    private       boolean          townyHookEnabled;
    @Getter
    private       boolean          factionsUUIDEnabled;
    @Getter
    private       boolean          residenceEnabled;
    @Getter
    private       boolean          griefPreventionEnabled;
    @Getter
    private       boolean          protectionStonesEnabled;
    @Getter
    private       boolean          griefDefenderEnabled;
    @Getter
    private       LandsIntegration landsIntegration;

    private boolean debug = false;

    public void debug(String message) {
        if (debug) {
            this.getLogger().info(message);
        }
    }

    public boolean areWorldsBlacklisted() {
        return worldsBlacklisted;
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

        this.worldGuardEnabled = (getServer().getPluginManager().getPlugin("WorldGuard") != null);
        this.townyHookEnabled =
                (getConfig().getBoolean("towny") && getServer().getPluginManager().getPlugin("Towny") != null);
        this.factionsUUIDEnabled = (getConfig().getBoolean("factionsuuid")
                && getServer().getPluginManager().getPlugin("Factions") != null);
        this.residenceEnabled =
                (getConfig().getBoolean("residence") && getServer().getPluginManager().getPlugin("Residence") != null);
        this.griefPreventionEnabled = (getConfig().getBoolean("grief-prevention")
                && getServer().getPluginManager().getPlugin("GriefPrevention") != null);
        this.protectionStonesEnabled = (getConfig().getBoolean("protections-stones")
                && getServer().getPluginManager().getPlugin("ProtectionStones") != null);
        this.griefDefenderEnabled = (getConfig().getBoolean("grief-defender")
                && getServer().getPluginManager().getPlugin("GriefDefender") != null);
        if (getConfig().getBoolean("lands") && getServer().getPluginManager().getPlugin("Lands") != null) {
            this.landsIntegration = new LandsIntegration(this);
        } else {
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
}