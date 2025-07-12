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
    
    public HealthDisplayManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setupHealthObjective();
    }
    
    // Register objective once
    private void setupHealthObjective() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        
        if (scoreboard.getObjective("health") == null) {
            Objective healthObj = scoreboard.registerNewObjective(
                    "health",
                    "health",
                    Component.text("❤", NamedTextColor.RED)
            );
            healthObj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }
    
    // Update player's health value
    private void updatePlayerHealth(Player player) {
        Duel duel = plugin.getDuelManager().getDuel(player);
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS && duel.getKit().getRule("health-indicators")) {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective objective = scoreboard.getObjective("health");
            if (objective != null) {
                Score score = objective.getScore(player.getName());
                score.setScore((int) Math.ceil(player.getHealth())); // Round up partial hearts
            }
        } else {
            // Hide health indicators if not in active duel or kit doesn't allow it
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective objective = scoreboard.getObjective("health");
            if (objective != null) {
                scoreboard.resetScores(player.getName());
            }
        }
    }
    
    // Update on join
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updatePlayerHealth(event.getPlayer());
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
}