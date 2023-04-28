package it.ale1x.votenight;

import java.io.File;
import java.util.Objects;

import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class VoteNight extends JavaPlugin {
    private VoteManager voteManager;

    private FileConfiguration config;


    public void onEnable() {

        System.out.println("[Votazione Notte] Attivato!");

        saveDefaultConfig();
        config = getConfig();
        FileConfiguration guiConfig = loadCustomConfig();

        voteManager = new VoteManager(this, config, guiConfig);

        registerCommands();
    }

    public void onDisable() {
        System.out.println("[Votazione Notte] Disattivato!");
    }


    /**
     *
     */
    protected YamlConfiguration loadCustomConfig() {
        File file = new File(getDataFolder(), "gui.yml");
        if (!file.exists())
            saveResource("gui.yml", false);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void registerCommands() {
        registerVotazioneNottePollCmd();
        registerVotazioneNotteCmd();
        registerVotazioneNotteReloadCmd();
    }

    private void registerVotazioneNottePollCmd() {
        PluginCommand votazioneNotteCmd = getCommand("vota-notte-poll");
        if (votazioneNotteCmd != null) {
            Objects.requireNonNull(votazioneNotteCmd).setExecutor(new VotazioneNotteCommand(this, voteManager, config));
        }
    }

    private void registerVotazioneNotteCmd() {
        PluginCommand votaCmd = getCommand("vota-notte");
        Objects.requireNonNull(votaCmd).setExecutor(new VotaCommand(voteManager));
    }

    private void registerVotazioneNotteReloadCmd() {
        PluginCommand reloadCmd = getCommand("votanotte-reload");
        Objects.requireNonNull(reloadCmd).setExecutor(new VotazioneReloadCommand(this, config));
    }

    public VoteManager getVoteManager() {
        return this.voteManager;
    }
}
