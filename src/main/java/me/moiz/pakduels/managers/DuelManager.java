package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.models.Duel;
import me.moiz.pakduels.models.DuelRequest;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelManager {
    private final PakDuelsPlugin plugin;
    private final Map<UUID, DuelRequest> duelRequests;
    private final Map<UUID, Duel> activeDuels;
    private final Map<Player, Duel> playerDuels;
    
    public DuelManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.duelRequests = new HashMap<>();
        this.activeDuels = new HashMap<>();
        this.playerDuels = new HashMap<>();
        
        // Clean up expired requests every 30 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredRequests();
            }
        }.runTaskTimer(plugin, 600L, 600L);
    }
    
    public void addDuelRequest(DuelRequest request) {
        duelRequests.put(request.getId(), request);
    }
    
    public DuelRequest getDuelRequest(Player challenger, Player challenged) {
        return duelRequests.values().stream()
                .filter(request -> request.getChallenger().equals(challenger) && request.getChallenged().equals(challenged))
                .findFirst()
                .orElse(null);
    }
    
    public void removeDuelRequest(DuelRequest request) {
        duelRequests.remove(request.getId());
    }
    
    public boolean isInDuel(Player player) {
        return playerDuels.containsKey(player);
    }
    
    public Duel getDuel(Player player) {
        return playerDuels.get(player);
    }
    
    public void startDuel(DuelRequest request) {
        Player player1 = request.getChallenger();
        Player player2 = request.getChallenged();
        Kit kit = request.getKit();
        
        // Find available arena
        Arena arena = plugin.getArenaManager().getAvailableArena(kit.getName());
        if (arena == null) {
            MessageUtils.sendMessage(player1, "arena-not-found");
            MessageUtils.sendMessage(player2, "arena-not-found");
            removeDuelRequest(request);
            return;
        }
        
        // Reserve arena
        arena.setReserved(true);
        
        // Create duel
        Duel duel = new Duel(player1, player2, kit, arena, request.getRounds());
        activeDuels.put(duel.getId(), duel);
        playerDuels.put(player1, duel);
        playerDuels.put(player2, duel);
        
        // Remove request
        removeDuelRequest(request);
        
        // Setup duel
        setupDuel(duel);
    }
    
    private void setupDuel(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        Arena arena = duel.getArena();
        Kit kit = duel.getKit();
        
        // Save original inventories and states
        duel.savePlayerInventory(player1);
        duel.savePlayerInventory(player2);
        
        // Teleport players
        player1.teleport(arena.getSpawnPoint1());
        player2.teleport(arena.getSpawnPoint2());
        
        // Make players invulnerable during setup
        player1.setInvulnerable(true);
        player2.setInvulnerable(true);
        
        // Apply kit
        applyKit(player1, kit);
        applyKit(player2, kit);
        
        // Set game mode
        player1.setGameMode(GameMode.SURVIVAL);
        player2.setGameMode(GameMode.SURVIVAL);
        
        // Setup scoreboard
        plugin.getScoreboardManager().setupDuelScoreboard(duel);
        
        // Start inventory countdown
        startInventoryCountdown(duel);
        
        MessageUtils.sendMessage(player1, "duel-started", "player", player2.getName());
        MessageUtils.sendMessage(player2, "duel-started", "player", player1.getName());
    }
    
    private void startInventoryCountdown(Duel duel) {
        duel.setState(Duel.DuelState.INVENTORY_COUNTDOWN);
        
        int countdownTime = plugin.getConfigManager().getInventoryCountdownTime();
        
        new BukkitRunnable() {
            int timeLeft = countdownTime;
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    // Save modified inventories
                    duel.savePlayerInventory(duel.getPlayer1());
                    duel.savePlayerInventory(duel.getPlayer2());
                    
                    // Start duel countdown
                    startDuelCountdown(duel);
                    cancel();
                    return;
                }
                
                if (timeLeft <= 5) {
                    Component title = Component.text(String.valueOf(timeLeft), NamedTextColor.RED);
                    Component subtitle = Component.text(plugin.getConfigManager().getMessageText("countdown-organize"), NamedTextColor.GRAY);
                    
                    Title titleObj = Title.title(title, subtitle);
                    duel.getPlayer1().showTitle(titleObj);
                    duel.getPlayer2().showTitle(titleObj);
                    
                    // Play sound
                    duel.getPlayer1().playSound(duel.getPlayer1().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    duel.getPlayer2().playSound(duel.getPlayer2().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void startDuelCountdown(Duel duel) {
        duel.setState(Duel.DuelState.STARTING);
        
        new BukkitRunnable() {
            int countdown = 3;
            
            @Override
            public void run() {
                if (countdown <= 0) {
                    duel.setState(Duel.DuelState.IN_PROGRESS);
                    
                    Component goTitle = Component.text(plugin.getConfigManager().getMessageText("countdown-go"), NamedTextColor.GREEN);
                    Title titleObj = Title.title(goTitle, Component.empty());
                    duel.getPlayer1().showTitle(titleObj);
                    duel.getPlayer2().showTitle(titleObj);
                    
                    // Remove invulnerability
                    duel.getPlayer1().setInvulnerable(false);
                    duel.getPlayer2().setInvulnerable(false);
                    
                    duel.getPlayer1().playSound(duel.getPlayer1().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    duel.getPlayer2().playSound(duel.getPlayer2().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    
                    plugin.getScoreboardManager().updateDuelScoreboard(duel);
                    cancel();
                    return;
                }
                
                Component title = Component.text(String.valueOf(countdown), NamedTextColor.YELLOW);
                Title titleObj = Title.title(title, Component.empty());
                duel.getPlayer1().showTitle(titleObj);
                duel.getPlayer2().showTitle(titleObj);
                
                duel.getPlayer1().playSound(duel.getPlayer1().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                duel.getPlayer2().playSound(duel.getPlayer2().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    public void endRound(Duel duel, Player winner) {
        if (duel.getState() != Duel.DuelState.IN_PROGRESS) return;
        
        duel.setState(Duel.DuelState.ROUND_ENDING);
        duel.incrementScore(winner);
        
        String message = plugin.getConfigManager().getMessageText("round-won")
                .replace("{round}", String.valueOf(duel.getCurrentRound()))
                .replace("{winner}", winner.getName());
        MessageUtils.sendRawMessage(duel.getPlayer1(), message);
        MessageUtils.sendRawMessage(duel.getPlayer2(), message);
        
        plugin.getScoreboardManager().updateDuelScoreboard(duel);
        
        if (duel.isFinished()) {
            endDuel(duel, duel.getWinner());
        } else {
            // Make players invulnerable during round transition
            duel.getPlayer1().setInvulnerable(true);
            duel.getPlayer2().setInvulnerable(true);
            
            // Start next round
            int roundDelay = plugin.getConfigManager().getRoundDelay();
            new BukkitRunnable() {
                @Override
                public void run() {
                    startNextRound(duel);
                }
            }.runTaskLater(plugin, roundDelay * 20L);
        }
    }
    
    private void startNextRound(Duel duel) {
        duel.setCurrentRound(duel.getCurrentRound() + 1);
        
        // Restore saved inventories
        duel.restorePlayerInventory(duel.getPlayer1());
        duel.restorePlayerInventory(duel.getPlayer2());
        
        // Heal players
        duel.getPlayer1().setHealth(20.0);
        duel.getPlayer2().setHealth(20.0);
        duel.getPlayer1().setFoodLevel(20);
        duel.getPlayer2().setFoodLevel(20);
        
        // Teleport to spawn points
        duel.getPlayer1().teleport(duel.getArena().getSpawnPoint1());
        duel.getPlayer2().teleport(duel.getArena().getSpawnPoint2());
        
        // Regenerate arena if enabled
        if (duel.getArena().isRegenerationEnabled()) {
            // TODO: Implement FAWE regeneration
        }
        
        // Start countdown
        startDuelCountdown(duel);
    }
    
    public void endDuel(Duel duel, Player winner) {
        duel.setState(Duel.DuelState.FINISHED);
        
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        // Announce winner
        if (winner != null) {
            MessageUtils.sendMessage(player1, "duel-ended", "winner", winner.getName());
            MessageUtils.sendMessage(player2, "duel-ended", "winner", winner.getName());
        } else {
            MessageUtils.sendMessage(player1, "duel-draw");
            MessageUtils.sendMessage(player2, "duel-draw");
        }
        
        // Cleanup
        cleanupDuel(duel);
    }
    
    private void cleanupDuel(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        // Remove from maps
        activeDuels.remove(duel.getId());
        playerDuels.remove(player1);
        playerDuels.remove(player2);
        
        // Unreserve arena
        duel.getArena().setReserved(false);
        
        // Remove scoreboards
        plugin.getScoreboardManager().removeDuelScoreboard(duel);
        
        // Restore player states
        restorePlayerState(player1);
        restorePlayerState(player2);
        
        // Teleport to spawn
        teleportToSpawn(player1);
        teleportToSpawn(player2);
    }
    
    private void restorePlayerState(Player player) {
        // Restore original inventory
        Duel duel = playerDuels.get(player);
        if (duel != null) {
            duel.restorePlayerInventory(player);
        }
        
        // Reset player state
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setGameMode(org.bukkit.GameMode.SURVIVAL);
        player.setInvulnerable(false);
        
        // Clear effects
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    private void teleportToSpawn(Player player) {
        if (plugin.getConfigManager().hasSpawnSet()) {
            String worldName = plugin.getConfigManager().getSpawnWorldName();
            double x = plugin.getConfigManager().getSpawnX();
            double y = plugin.getConfigManager().getSpawnY();
            double z = plugin.getConfigManager().getSpawnZ();
            float yaw = plugin.getConfigManager().getSpawnYaw();
            float pitch = plugin.getConfigManager().getSpawnPitch();
            
            org.bukkit.World world = Bukkit.getWorld(worldName);
            if (world != null) {
                Location spawnLoc = new Location(world, x, y, z, yaw, pitch);
                player.teleport(spawnLoc);
            }
        }
    }
    
    public void endAllDuels() {
        for (Duel duel : new ArrayList<>(activeDuels.values())) {
            endDuel(duel, null);
        }
    }
    
    private void applyKit(Player player, Kit kit) {
        player.getInventory().clear();
        player.getInventory().setContents(kit.getContents());
        player.getInventory().setArmorContents(kit.getArmorContents());
        player.getInventory().setItemInOffHand(kit.getOffHand());
        
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
    }
    
    private void cleanupExpiredRequests() {
        duelRequests.values().removeIf(DuelRequest::isExpired);
    }
}