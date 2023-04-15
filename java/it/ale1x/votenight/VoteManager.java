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
    private VoteNight plugin;

    private boolean votingInProgress = false;

    private int yesVotes = 0;

    private int noVotes = 0;

    private int totalVotes = 0;

    private Map<UUID, Boolean> playersVoted = new HashMap<>();

    private Inventory voteInventory;

    private FileConfiguration config;

    private FileConfiguration guiConfig;

    private String worldName;
    private int checkVoteTicks;
    private BukkitTask checkVoteTask;
    private int voteCooldownTicks;

    public VoteManager(VoteNight plugin, FileConfiguration config, FileConfiguration guiConfig) {
        this.plugin = plugin;
        this.config = config;
        this.guiConfig = guiConfig;
        this.votingInProgress = false;
        plugin.getServer().getPluginManager().registerEvents(this, (Plugin)plugin);
        createVoteInventory();
        this.worldName = config.getString("mondo-da-controllare");
        this.checkVoteTicks = config.getInt("tick-da-controllare", 0);
        this.voteCooldownTicks = config.getInt("vote-cooldown-ticks", 6000);
        if (checkVoteTicks > 0 && worldName != null) {
            startCheckVoteTask();
        }
    }


    public void startVote(int durationSeconds) {
        if (!this.votingInProgress) {
            this.votingInProgress = true;
            this.yesVotes = 0;
            this.noVotes = 0;
            this.totalVotes = 0;
            this.playersVoted.clear();

            broadcastMessage(config.getString("prefix") + config.getString("messaggio-inizio-votazione"));


            (new BukkitRunnable() {
                public void run() {
                    VoteManager.this.votingInProgress = false;
                    double requiredPercentage = VoteManager.this.config.getDouble("percentuale-richiesta");

                    if(VoteManager.this.totalVotes == 0) {
                        VoteManager.this.totalVotes = 1;
                    }

                    double yesPercentage = VoteManager.this.yesVotes / VoteManager.this.totalVotes * 100.0D;
                    if (yesPercentage >= requiredPercentage) {
                        List<String> worldsToChange = VoteManager.this.config.getStringList("mondi-da-cambiare");
                        for (String worldName : worldsToChange) {
                            World world = Bukkit.getWorld(worldName);
                            if (world != null)
                                world.setTime(config.getLong("tick-da-impostare"));
                        }
                        broadcastMessage(config.getString("prefix") + config.getString("messaggio-giorno"));
                    } else {
                        broadcastMessage(config.getString("prefix") + config.getString("messaggio-notte"));
                    }
                }
            }).runTaskLater((Plugin)this.plugin, (durationSeconds * 20));
        }
    }

    private void startCheckVoteTask() {
        checkVoteTask = new BukkitRunnable() {
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

    private void broadcastMessage(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
    public void openVoteGUI(Player player) {
        if (this.votingInProgress) {
            player.openInventory(this.voteInventory);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-no-votazione")));
        }
    }

    private void createVoteInventory() {
        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', this.guiConfig.getString("inventario.titolo"));
        int inventorySize = this.guiConfig.getInt("inventario.dimensioni");
        this.voteInventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);
        ConfigurationSection itemsSection = this.guiConfig.getConfigurationSection("inventario.items");
        for (String key : itemsSection.getKeys(false)) {
            String materialName = itemsSection.getString(key + ".materiale");
            String displayName = itemsSection.getString(key + ".display-name");
            int slot = itemsSection.getInt(key + ".slot");
            ItemStack item = createCustomItem(materialName, displayName);
            if (item != null)
                this.voteInventory.setItem(slot, item);
        }
    }

    private ItemStack createCustomItem(String materialName, String displayName) {
        Material material = Material.matchMaterial(materialName);
        if (material == null)
            return null;
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().equals(this.voteInventory)) {
            event.setCancelled(true);
            Player player = (Player)event.getWhoClicked();
            UUID playerUUID = player.getUniqueId();
            if (!this.playersVoted.containsKey(playerUUID)) {
                int slot = event.getRawSlot();
                if (slot == 3) {
                    this.yesVotes++;
                    this.totalVotes++;
                    this.playersVoted.put(playerUUID, Boolean.valueOf(true));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-votato-si")));
                } else if (slot == 5) {
                    this.noVotes++;
                    this.totalVotes++;
                    this.playersVoted.put(playerUUID, Boolean.valueOf(false));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-votato-no")));
                }
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + this.config.getString("messaggio-votato-gia")));
            }
            player.closeInventory();
        }
    }

    public void reloadConfig() {
        this.guiConfig = this.plugin.loadCustomConfig("gui.yml");
        createVoteInventory();
    }
}
