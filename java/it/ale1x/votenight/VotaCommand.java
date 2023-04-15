package it.ale1x.votenight;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class VotaCommand implements CommandExecutor {
    private VoteNight plugin;

    private VoteManager voteManager;

    private FileConfiguration config;

    public VotaCommand(VoteNight plugin, VoteManager voteManager, FileConfiguration config) {
        this.plugin = plugin;
        this.voteManager = voteManager;
        this.config = config;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            this.voteManager.openVoteGUI(player);
        }
        return true;
    }
}
