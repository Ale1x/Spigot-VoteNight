package it.ale1x.votenight;

import java.util.*;

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

public class VoteManager implements Listener {

    private final VoteNight plugin;
    private final FileConfiguration config;
    private FileConfiguration guiConfig;
    private final Map<UUID, Boolean> playersVoted;
    private final Inventory voteInventory;
    private final String worldName;
    private final int checkVoteTicks;
    private final int voteCooldownTicks;

    private boolean votingInProgress;
    private int yesVotes;
    private int totalVotes;

    public VoteManager(VoteNight plugin, FileConfiguration config, FileConfiguration guiConfig) {
        this.plugin = plugin;
        this.config = config;
        this.guiConfig = guiConfig;
        this.playersVoted = new HashMap<>();
        this.voteInventory = createVoteInventory();
        this.worldName = config.getString("mondo-da-controllare");
        this.checkVoteTicks = config.getInt("tick-da-controllare", 0);
        this.voteCooldownTicks = config.getInt("vote-cooldown-ticks", 6000);

        plugin.getServer().getPluginManager().registerEvents(this, (Plugin) plugin);
        startCheckVoteTask();
    }

    public void startVote(int durationSeconds) {
        if (!votingInProgress) {
            initializeVoting();
            broadcastMessage(config.getString("prefix") + config.getString("messaggio-inizio-votazione"));
            schedulePollEnd(durationSeconds);
        }
    }

    private void initializeVoting() {
        votingInProgress = true;
        yesVotes = 0;
        totalVotes = 0;
        playersVoted.clear();
    }

    private void schedulePollEnd(int durationSeconds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                endVoting();
            }
        }.runTaskLater((Plugin) plugin, (durationSeconds * 20L));
    }

    private void endVoting() {
        votingInProgress = false;
        double requiredPercentage = config.getDouble("percentuale-richiesta");

        if (totalVotes == 0) {
            totalVotes = 1;
        }

        double yesPercentage = (double) yesVotes / totalVotes * 100.0D;
        if (yesPercentage >= requiredPercentage) {
            performTimeChange();
        } else {
            broadcastMessage(config.getString("prefix") + config.getString("messaggio-notte"));
        }
    }

    private void startCheckVoteTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = Bukkit.getWorld(Objects.requireNonNull(worldName));
                if (world != null && world.getTime() >= checkVoteTicks && !votingInProgress) {
                    int duration = config.getInt("durata-votazione");
                    startVote(duration);
                    this.cancel();
                    scheduleCheckVoteTaskRestart();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void scheduleCheckVoteTaskRestart() {
        new BukkitRunnable() {
            @Override
            public void run() {
                startCheckVoteTask();
            }
        }.runTaskLater(plugin, voteCooldownTicks);
    }
    public void openVoteGUI(Player player) {
        if (votingInProgress) {
            player.openInventory(voteInventory);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-no-votazione")));
        }
    }

    private Inventory createVoteInventory() {
        String inventoryTitle = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(guiConfig.getString("inventario.titolo")));
        int inventorySize = guiConfig.getInt("inventario.dimensioni");
        Inventory inventory = Bukkit.createInventory(null, inventorySize, inventoryTitle);
        ConfigurationSection itemsSection = guiConfig.getConfigurationSection("inventario.items");
        for (String key : Objects.requireNonNull(itemsSection).getKeys(false)) {
            String materialName = itemsSection.getString(key +
                    ".materiale");
            String displayName = itemsSection.getString(key + ".display-name");
            int slot = itemsSection.getInt(key + ".slot");
            ItemStack item = createCustomItem(materialName,
                    displayName);
            if (item != null)
                inventory.setItem(slot, item);
        }
        return inventory;
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
        if (event.getInventory().equals(voteInventory)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            UUID playerUUID = player.getUniqueId();
            if (!playersVoted.containsKey(playerUUID)) {
                processVote(player, event.getRawSlot());
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-votato-gia")));
                player.closeInventory();
            }
        }
    }

    private void processVote(Player player, int slot) {
        int yesSlot = guiConfig.getInt("inventario.items.yes.slot");
        int noSlot = guiConfig.getInt("inventario.items.no.slot");
        UUID playerUUID = player.getUniqueId();

        if (slot == yesSlot) {
            yesVotes++;
            totalVotes++;
            playersVoted.put(playerUUID, true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-votato-si")));
        } else if (slot == noSlot) {
            totalVotes++;
            playersVoted.put(playerUUID, false);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("prefix") + config.getString("messaggio-votato-no")));
        }
    }

    public void reloadConfig() {
        guiConfig = plugin.loadCustomConfig();
        createVoteInventory();
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
            if (player != null) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}

