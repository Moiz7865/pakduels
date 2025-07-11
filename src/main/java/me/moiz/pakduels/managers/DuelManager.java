package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.models.Duel;
import me.moiz.pakduels.models.DuelRequest;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuelManager {
    private final PakDuelsPlugin plugin;
    private final Map<UUID, Duel> activeDuels;
    private final Map<UUID, DuelRequest> pendingRequests;
    private final Map<UUID, UUID> playerDuelMap; // Player UUID -> Duel UUID
    
    public DuelManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.activeDuels = new ConcurrentHashMap<>();
        this.pendingRequests = new ConcurrentHashMap<>();
        this.playerDuelMap = new ConcurrentHashMap<>();
        
        // Start cleanup task for expired requests
        startCleanupTask();
    }
    
    private void startCleanupTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<DuelRequest> iterator = pendingRequests.values().iterator();
            while (iterator.hasNext()) {
                DuelRequest request = iterator.next();
                if (request.isExpired()) {
                    iterator.remove();
                    MessageUtils.sendMessage(request.getChallenger(), "&cDuel request to " + request.getChallenged().getName() + " expired.");
                    MessageUtils.sendMessage(request.getChallenged(), "&cDuel request from " + request.getChallenger().getName() + " expired.");
                }
            }
        }, 20L, 20L); // Run every second
    }
    
    public void addDuelRequest(DuelRequest request) {
        pendingRequests.put(request.getId(), request);
    }
    
    public void removeDuelRequest(UUID requestId) {
        pendingRequests.remove(requestId);
    }
    
    public DuelRequest getDuelRequest(Player challenger, Player challenged) {
        return pendingRequests.values().stream()
                .filter(request -> request.getChallenger().equals(challenger) && request.getChallenged().equals(challenged))
                .findFirst()
                .orElse(null);
    }
    
    public boolean startDuel(Player player1, Player player2, Kit kit, int rounds) {
        // Find available arena
        Arena arena = plugin.getArenaManager().getAvailableArena();
        if (arena == null) {
            MessageUtils.sendMessage(player1, "&cNo available arenas!");
            MessageUtils.sendMessage(player2, "&cNo available arenas!");
            return false;
        }
        
        // Check if kit is allowed in arena
        if (!arena.getAllowedKits().isEmpty() && !arena.getAllowedKits().contains(kit.getName())) {
            MessageUtils.sendMessage(player1, "&cThis kit is not allowed in available arenas!");
            MessageUtils.sendMessage(player2, "&cThis kit is not allowed in available arenas!");
            return false;
        }
        
        // Create duel
        Duel duel = new Duel(player1, player2, kit, arena, rounds);
        activeDuels.put(duel.getId(), duel);
        playerDuelMap.put(player1.getUniqueId(), duel.getId());
        playerDuelMap.put(player2.getUniqueId(), duel.getId());
        
        // Mark arena as in use
        arena.setInUse(true);
        
        // Save arena schematic if regeneration is enabled
        if (arena.isRegenerationEnabled()) {
            plugin.getArenaManager().saveSchematic(arena);
        }
        
        // Setup players
        setupPlayerForDuel(player1, kit);
        setupPlayerForDuel(player2, kit);
        
        // Teleport players to spawn points
        player1.teleport(arena.getSpawnPoint1());
        player2.teleport(arena.getSpawnPoint2());
        
        // Start duel
        duel.setState(Duel.DuelState.STARTING);
        startDuelCountdown(duel);
        
        // Setup scoreboards
        plugin.getScoreboardManager().setupDuelScoreboard(duel);
        
        return true;
    }
    
    private void setupPlayerForDuel(Player player, Kit kit) {
        // Clear player
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setGameMode(GameMode.SURVIVAL);
        
        // Give kit items
        player.getInventory().setContents(kit.getContents());
        player.getInventory().setArmorContents(kit.getArmorContents());
        player.getInventory().setItemInOffHand(kit.getOffHand());
        
        player.updateInventory();
    }
    
    private void startDuelCountdown(Duel duel) {
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        MessageUtils.sendMessage(player1, "&aDuel starting in 3 seconds...");
        MessageUtils.sendMessage(player2, "&aDuel starting in 3 seconds...");
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            MessageUtils.sendMessage(player1, "&62...");
            MessageUtils.sendMessage(player2, "&62...");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                MessageUtils.sendMessage(player1, "&c1...");
                MessageUtils.sendMessage(player2, "&c1...");
                
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    MessageUtils.sendMessage(player1, "&aGO!");
                    MessageUtils.sendMessage(player2, "&aGO!");
                    
                    duel.setState(Duel.DuelState.IN_PROGRESS);
                    plugin.getScoreboardManager().updateDuelScoreboard(duel);
                    
                }, 20L);
            }, 20L);
        }, 20L);
    }
    
    public void endRound(Duel duel, Player winner) {
        if (duel.getState() != Duel.DuelState.IN_PROGRESS) {
            return;
        }
        
        duel.setState(Duel.DuelState.ROUND_ENDING);
        duel.incrementScore(winner);
        
        Player loser = duel.getOpponent(winner);
        
        MessageUtils.sendMessage(winner, "&aYou won round " + duel.getCurrentRound() + "!");
        MessageUtils.sendMessage(loser, "&cYou lost round " + duel.getCurrentRound() + "!");
        
        plugin.getScoreboardManager().updateDuelScoreboard(duel);
        
        if (duel.isFinished()) {
            // Duel is finished
            Bukkit.getScheduler().runTaskLater(plugin, () -> endDuel(duel, winner), 40L);
        } else {
            // Start next round
            duel.setCurrentRound(duel.getCurrentRound() + 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> startNextRound(duel), 60L);
        }
    }
    
    private void startNextRound(Duel duel) {
        if (duel.getState() != Duel.DuelState.ROUND_ENDING) {
            return;
        }
        
        Player player1 = duel.getPlayer1();
        Player player2 = duel.getPlayer2();
        
        // Reset players
        setupPlayerForDuel(player1, duel.getKit());
        setupPlayerForDuel(player2, duel.getKit());
        
        // Teleport players back to spawn points
        player1.teleport(duel.getArena().getSpawnPoint1());
        player2.teleport(duel.getArena().getSpawnPoint2());
        
        // Regenerate arena if needed
        if (duel.getArena().isRegenerationEnabled()) {
            plugin.getArenaManager().pasteSchematic(duel.getArena());
        }
        
        MessageUtils.sendMessage(player1, "&aRound " + duel.getCurrentRound() + " starting...");
        MessageUtils.sendMessage(player2, "&aRound " + duel.getCurrentRound() + " starting...");
        
        startDuelCountdown(duel);
    }
    
    public void endDuel(Duel duel, Player winner) {
        duel.setState(Duel.DuelState.FINISHED);
        
        Player loser = duel.getOpponent(winner);
        
        MessageUtils.sendMessage(winner, "&aYou won the duel!");
        MessageUtils.sendMessage(loser, "&cYou lost the duel!");
        
        // Broadcast result
        String message = "&6" + winner.getName() + " &adefeated &6" + loser.getName() + " &ain a duel using &f" + duel.getKit().getName() + " &akit!";
        Bukkit.getOnlinePlayers().forEach(player -> MessageUtils.sendMessage(player, message));
        
        // Cleanup
        cleanupDuel(duel);
    }
    
    public void endDuel(Duel duel) {
        duel.setState(Duel.DuelState.FINISHED);
        
        MessageUtils.sendMessage(duel.getPlayer1(), "&cDuel ended!");
        MessageUtils.sendMessage(duel.getPlayer2(), "&cDuel ended!");
        
        cleanupDuel(duel);
    }
    
    private void cleanupDuel(Duel duel) {
        // Remove from maps
        activeDuels.remove(duel.getId());
        playerDuelMap.remove(duel.getPlayer1().getUniqueId());
        playerDuelMap.remove(duel.getPlayer2().getUniqueId());
        
        // Mark arena as not in use
        duel.getArena().setInUse(false);
        
        // Remove scoreboards
        plugin.getScoreboardManager().removeDuelScoreboard(duel);
        
        // Reset players (optional - might want to restore their previous state)
        resetPlayerAfterDuel(duel.getPlayer1());
        resetPlayerAfterDuel(duel.getPlayer2());
    }
    
    private void resetPlayerAfterDuel(Player player) {
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setGameMode(GameMode.SURVIVAL);
        
        // Teleport to spawn or previous location
        player.teleport(player.getWorld().getSpawnLocation());
    }
    
    public boolean isInDuel(Player player) {
        return playerDuelMap.containsKey(player.getUniqueId());
    }
    
    public Duel getDuel(Player player) {
        UUID duelId = playerDuelMap.get(player.getUniqueId());
        return duelId != null ? activeDuels.get(duelId) : null;
    }
    
    public Duel getDuel(UUID duelId) {
        return activeDuels.get(duelId);
    }
    
    public Collection<Duel> getActiveDuels() {
        return new ArrayList<>(activeDuels.values());
    }
    
    public void endAllDuels() {
        for (Duel duel : new ArrayList<>(activeDuels.values())) {
            endDuel(duel);
        }
    }
}