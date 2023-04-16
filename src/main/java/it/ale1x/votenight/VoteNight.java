package it.ale1x.votenight;

import java.io.File;
import java.util.Collections;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class VoteNight extends JavaPlugin {
    private VoteManager voteManager;

    private FileConfiguration config;

    private FileConfiguration guiConfig;

    public void onEnable() {

        System.out.println("[Votazione Notte] Attivato!");

        saveDefaultConfig();
        config = getConfig();
        guiConfig = loadCustomConfig("gui.yml");

        voteManager = new VoteManager(this, config, guiConfig);

        registerCommands();
    }

    public void onDisable() {
        System.out.println("[Votazione Notte] Disattivato!");
    }

    protected FileConfiguration loadCustomConfig(String filename) {
        File file = new File(getDataFolder(), filename);
        if (!file.exists())
            saveResource(filename, false);
        return (FileConfiguration)YamlConfiguration.loadConfiguration(file);
    }

    private void registerCommands() {
        registerVotazioneNottePollCmd();
        registerVotazioneNotteCmd();
        registerVotazioneNotteReloadCmd();
    }

    private void registerVotazioneNottePollCmd() {
        PluginCommand votazioneNotteCmd = getCommand("vota-notte-poll");
        votazioneNotteCmd.setExecutor(new VotazioneNotteCommand(this, voteManager, config));
    }

    private void registerVotazioneNotteCmd() {
        PluginCommand votaCmd = getCommand("vota-notte");
        votaCmd.setExecutor(new VotaCommand(this, voteManager, config));
    }

    private void registerVotazioneNotteReloadCmd() {
        PluginCommand reloadCmd = getCommand("votanotte-reload");
        reloadCmd.setExecutor(new VotazioneReloadCommand(this, config));
    }

    public VoteManager getVoteManager() {
        return this.voteManager;
    }
}
