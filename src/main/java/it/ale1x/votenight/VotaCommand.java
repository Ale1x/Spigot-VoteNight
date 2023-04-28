package it.ale1x.votenight;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VotaCommand implements CommandExecutor {
    private final VoteManager voteManager;

    public VotaCommand(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args)
    {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            this.voteManager.openVoteGUI(player);
        }
        return true;
    }
}
