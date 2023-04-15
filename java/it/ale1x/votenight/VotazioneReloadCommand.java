package it.ale1x.votenight;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class VotazioneReloadCommand implements CommandExecutor {
    private VoteNight plugin;

    private FileConfiguration config;

    public VotazioneReloadCommand(VoteNight plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (player.hasPermission(config.getString("permesso-reload"))) {
                this.plugin.reloadConfig();
                this.config = this.plugin.getConfig();
                this.plugin.getVoteManager().reloadConfig();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-reload")));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-no-permessi")));
            }
        } else {
            this.plugin.reloadConfig();
            this.config = this.plugin.getConfig();
            this.plugin.getVoteManager().reloadConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-reload")));
        }
        return true;
    }
}
