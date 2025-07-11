package me.moiz.pakduels.listeners;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {
    private final PakDuelsPlugin plugin;
    
    public DuelListener(PakDuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Duel duel = plugin.getDuelManager().getDuel(event.getEntity());
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            // Player died in duel
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
            
            // Winner is the opponent
            plugin.getDuelManager().endRound(duel, duel.getOpponent(event.getEntity()));
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Duel duel = plugin.getDuelManager().getDuel(event.getPlayer());
        if (duel != null) {
            // Player quit during duel - opponent wins
            plugin.getDuelManager().endDuel(duel, duel.getOpponent(event.getPlayer()));
        }
    }
}