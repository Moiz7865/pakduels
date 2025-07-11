package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.models.Duel;
import me.moiz.pakduels.models.DuelRequest;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager {
    private final PakDuelsPlugin plugin;
    private final Map<UUID, DuelRequest> duelRequests;
    private final Map<UUID, Duel> activeDuels;
    private final Map<Player, Duel> playerDuels;
    
    public DuelManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.duelRequests = new ConcurrentHashMap<>();
        this.activeDuels = new ConcurrentHashMap<>();
        this.playerDuels = new ConcurrentHashMap<>();
        
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
            MessageUtils.sendMessage(player1, "&cNo available arena found for this kit!");
            MessageUtils.sendMessage(player2, "&cNo available arena found for this kit!");
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
        savePlayerState(player1);
        savePlayerState(player2);
        
        // Teleport players
        player1.teleport(arena.getSpawnPoint1());
        player2.teleport(arena.getSpawnPoint2());
        
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
        
        MessageUtils.sendMessage(player1, "&aDuel started against &f" + player2.getName() + "&a!");
        MessageUtils.sendMessage(player2, "&aDuel started against &f" + player1.getName() + "&a!");
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
                    // Show countdown
                    String title = "&e" + timeLeft;
                    MessageUtils.sendMessage(duel.getPlayer1(), title);
                    MessageUtils.sendMessage(duel.getPlayer2(), title);
                    
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
                    MessageUtils.sendMessage(duel.getPlayer1(), "&a&lGO!");
                    MessageUtils.sendMessage(duel.getPlayer2(), "&a&lGO!");
                    
                    duel.getPlayer1().playSound(duel.getPlayer1().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    duel.getPlayer2().playSound(duel.getPlayer2().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    
                    plugin.getScoreboardManager().updateDuelScoreboard(duel);
                    cancel();
                    return;
                }
                
                String title = "&e&l" + countdown;
                MessageUtils.sendMessage(duel.getPlayer1(), title);
                MessageUtils.sendMessage(duel.getPlayer2(), title);
                
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
        
        MessageUtils.sendMessage(duel.getPlayer1(), "&6Round " + duel.getCurrentRound() + " won by &f" + winner.getName() + "&6!");
        MessageUtils.sendMessage(duel.getPlayer2(), "&6Round " + duel.getCurrentRound() + " won by &f" + winner.getName() + "&6!");
        
        plugin.getScoreboardManager().updateDuelScoreboard(duel);
        
        if (duel.isFinished()) {
            endDuel(duel, duel.getWinner());
        } else {
            // Start next round
            new BukkitRunnable() {
                @Override
                public void run() {
                    startNextRound(duel);
                }
            }.runTaskLater(plugin, 60L); // 3 second delay
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
            MessageUtils.sendMessage(player1, "&6&l" + winner.getName() + " &a&lwins the duel!");
            MessageUtils.sendMessage(player2, "&6&l" + winner.getName() + " &a&lwins the duel!");
        } else {
            MessageUtils.sendMessage(player1, "&6&lDuel ended in a draw!");
            MessageUtils.sendMessage(player2, "&6&lDuel ended in a draw!");
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
        
        // Teleport to spawn (if configured)
        // TODO: Implement spawn teleportation
    }
    
    public void endAllDuels() {
        for (Duel duel : new ArrayList<>(activeDuels.values())) {
            endDuel(duel, null);
        }
    }
    
    private void savePlayerState(Player player) {
        // TODO: Save player state (inventory, location, etc.)
    }
    
    private void restorePlayerState(Player player) {
        // TODO: Restore player state
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