package it.ale1x.votenight;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class VotazioneReloadCommand implements CommandExecutor {
    private final VoteNight plugin;

    private FileConfiguration config;

    public VotazioneReloadCommand(VoteNight plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args)
    {

        if (sender instanceof Player) {
            Player player = (Player)sender;
            playerPerformCommand(player);
        } else{
            consolePerformCommand();
        }
        return true;
    }

    private void playerPerformCommand(Player p) {

        if (!p.hasPermission(Objects.requireNonNull(config.getString("permesso-reload")))) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-no-permessi")));
        } else {
            playerHasPermissions();
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-reload")));
        }
    }

    private void consolePerformCommand() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getVoteManager().reloadConfig();
        System.out.println("Config ricaricata!");
    }

    private void playerHasPermissions() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.getVoteManager().reloadConfig();
    }
}
