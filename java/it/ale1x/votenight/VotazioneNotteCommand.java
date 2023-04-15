package it.ale1x.votenight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class VotazioneNotteCommand implements CommandExecutor {
    private VoteNight plugin;

    private VoteManager voteManager;

    private FileConfiguration config;

    public VotazioneNotteCommand(VoteNight plugin, VoteManager voteManager, FileConfiguration config) {
        this.plugin = plugin;
        this.voteManager = voteManager;
        this.config = config;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (player.hasPermission(config.getString("permesso-votazione"))) {
                int duration = this.config.getInt("durata-votazione");
                this.voteManager.startVote(duration);
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-no-permessi")));
            }
        }
        return true;
    }
}
