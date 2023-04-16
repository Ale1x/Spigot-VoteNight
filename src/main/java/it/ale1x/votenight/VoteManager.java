package it.ale1x.votenight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class VoteManager implements Listener {
    private final VoteNight plugin;
    private boolean votingInProgress;
    private int yesVotes;
    private int noVotes;
    private int totalVotes;
    private final Map<UUID, Boolean> playersVoted = new HashMap<>();
    private Inventory voteInventory;
    private FileConfiguration config;
    private FileConfiguration guiConfig;
    private final String worldName;
    private final int checkVoteTicks;
    private final int voteCooldownTicks;

    public VoteManager(VoteNight plugin, FileConfiguration config, FileConfiguration guiConfig) {

        this.plugin = plugin;
        this.config = config;
        this.guiConfig = guiConfig;
        this.yesVotes = 0;
        this.noVotes = 0;
        this.totalVotes = 0;
        votingInProgress = false;
        this.votingInProgress = false;
        this.worldName = config.getString("mondo-da-controllare");
        this.checkVoteTicks = config.getInt("tick-da-controllare", 0);
        this.voteCooldownTicks = config.getInt("vote-cooldown-ticks", 6000);

        plugin.getServer().getPluginManager().registerEvents(this, (Plugin)plugin);
        createVoteInventory();

        if (checkVoteTicks > 0 && worldName != null) {
            startCheckVoteTask();
        }

    }


    public void startVote(int durationSeconds) {
        if (!votingInProgress) {
            votingInProgress = true;
            yesVotes = 0;
            noVotes = 0;
            totalVotes = 0;
            playersVoted.clear();

            broadcastMessage(config.getString("prefix") + config.getString("messaggio-inizio-votazione"));

            peformPoll(durationSeconds);

        }
    }

    private void startCheckVoteTask() {
        BukkitTask checkVoteTask = new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld(worldName);
                if (world != null && world.getTime() >= checkVoteTicks && !votingInProgress) {
                    int duration = config.getInt("durata-votazione");
                    startVote(duration);
                    this.cancel();
                    (new BukkitRunnable() {
                        @Override
                        public void run() {
                            startCheckVoteTask();
                        }
                    }).runTaskLater(plugin, voteCooldownTicks);
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Controlla ogni secondo (20 tick)
    }

    public void openVoteGUI(Player player) {
        if (votingInProgress) {
            player.openInventory(voteInventory);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-no-votazione")));
        }
    }

    private void createVoteInventory() {
        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("inventario.titolo"));
        int inventorySize = guiConfig.getInt("inventario.dimensioni");
        voteInventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);
        ConfigurationSection itemsSection = guiConfig.getConfigurationSection("inventario.items");
        for (String key : itemsSection.getKeys(false)) {
            String materialName = itemsSection.getString(key + ".materiale");
            String displayName = itemsSection.getString(key + ".display-name");
            int slot = itemsSection.getInt(key + ".slot");
            ItemStack item = createCustomItem(materialName, displayName);
            if (item != null)
                voteInventory.setItem(slot, item);
        }
    }

    private ItemStack createCustomItem(String materialName, String displayName) {
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            return null;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        int yesSlot = guiConfig.getInt("inventario.items.yes.slot");
        int noSlot = guiConfig.getInt("inventario.items.no.slot");

        if (event.getInventory().equals(voteInventory)) {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            UUID playerUUID = player.getUniqueId();
            if (!playersVoted.containsKey(playerUUID)) {
                int slot = event.getRawSlot();
                if (slot == yesSlot) {
                    playerVotedYes(player);
                    playersVoted.put(playerUUID, true);
                } else if (slot == noSlot) {
                    playerVotedNo(player);
                    playersVoted.put(playerUUID, false);
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-votato-gia")));
            }
            player.closeInventory();
        }
    }

    private void playerVotedYes(Player p) {
        yesVotes++;
        totalVotes++;
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-votato-si")));
    }

    private void playerVotedNo(Player p) {
        noVotes++;
        totalVotes++;
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-votato-no")));
    }

    public void reloadConfig() {
        this.guiConfig = plugin.loadCustomConfig("gui.yml");
        createVoteInventory();
    }

    private void peformPoll(int durationSeconds) {

        (new BukkitRunnable() {
            public void run() {
                votingInProgress = false;
                double requiredPercentage = config.getDouble("percentuale-richiesta");

                if(totalVotes == 0) {
                    totalVotes = 1;
                }

                double yesPercentage = (double) yesVotes / totalVotes * 100.0D;
                if (yesPercentage >= requiredPercentage)
                    performTimeChange();
                else
                    broadcastMessage(config.getString("prefix") + config.getString("messaggio-notte"));
            }
        }).runTaskLater((Plugin)plugin, (durationSeconds * 20L));
    }

    private void performTimeChange() {

        List<String> worldsToChange = config.getStringList("mondi-da-cambiare");
        for (String worldName : worldsToChange) {
            World world = Bukkit.getWorld(worldName);
            if (world != null)
                world.setTime(config.getLong("tick-da-impostare"));
        }

        broadcastMessage(config.getString("prefix") + config.getString("messaggio-giorno"));
    }

    private void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}
