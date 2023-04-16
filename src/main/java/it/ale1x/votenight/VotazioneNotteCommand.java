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
            performCommand(player);
        }
        return true;
    }

    private void performCommand(Player p) {
        if(p.hasPermission(config.getString("permesso-votazione"))) {
            int durataVotazione = config.getInt("durata-votazione");
            voteManager.startVote(durataVotazione);
        }else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix" + config.getString("messaggio-no-permessi"))));
        }
    }
}
