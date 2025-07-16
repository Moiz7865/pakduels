package me.moiz.pakduels.utils;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.*;

public class HealthDisplayManager implements Listener {
    private final PakDuelsPlugin plugin;
    private Objective healthObjective;
    
    public HealthDisplayManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupHealthObjective();
    }
    
    // Register objective once
    private void setupHealthObjective() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        
        // Remove existing objective if it exists
        Objective existing = scoreboard.getObjective("health");
        if (existing != null) {
            existing.unregister();
        }
        
        // Create new objective
        healthObjective = scoreboard.registerNewObjective(
                "health",
                Criteria.HEALTH,
                Component.text("❤", NamedTextColor.RED)
        );
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }
    
    // Update player's health value
    private void updatePlayerHealth(Player player) {
        if (healthObjective == null) {
            setupHealthObjective();
        }
        
        Duel duel = plugin.getDuelManager().getDuel(player);
        
        // Only show health indicators if player is in active duel AND kit allows it
        if (duel != null && 
            duel.getState() == Duel.DuelState.IN_PROGRESS && 
            duel.getKit().getRule("health-indicators")) {
            
            try {
                Score score = healthObjective.getScore(player.getName());
                score.setScore((int) Math.ceil(player.getHealth())); // Round up partial hearts
            } catch (IllegalStateException e) {
                // Score is read-only, ignore this error
                plugin.getLogger().fine("Could not update health score for " + player.getName() + ": " + e.getMessage());
            }
        } else {
            // Hide health indicators if not in active duel or kit doesn't allow it
            try {
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                scoreboard.resetScores(player.getName());
            } catch (Exception e) {
                // Ignore errors when resetting scores
                plugin.getLogger().fine("Could not reset health score for " + player.getName() + ": " + e.getMessage());
            }
        }
    }
    
    // Update on join
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerHealth(event.getPlayer()), 20L);
    }
    
    // Update on respawn
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerHealth(event.getPlayer()), 20L);
    }
    
    // Update on damage
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerHealth(player), 1L);
        }
    }
    
    public void cleanup() {
        if (healthObjective != null) {
            healthObjective.unregister();
            healthObjective = null;
        }
    }
}