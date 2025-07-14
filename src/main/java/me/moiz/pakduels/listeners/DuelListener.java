package me.moiz.pakduels.listeners;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Duel;
import me.moiz.pakduels.models.Kit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
            
            // Store death location for respawn
            Location deathLoc = event.getEntity().getLocation();
            
            // Respawn at death location after a short delay
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    event.getEntity().spigot().respawn();
                    event.getEntity().teleport(deathLoc);
                }
            }.runTaskLater(plugin, 1L);
            
            // Winner is the opponent
            plugin.getDuelManager().endRound(duel, duel.getOpponent(event.getEntity()));
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Duel duel = plugin.getDuelManager().getDuel(event.getPlayer());
        if (duel != null) {
            Player quitter = event.getPlayer();
            Player opponent = duel.getOpponent(quitter);
            
            // Announce that opponent wins due to quit
            MessageUtils.sendRawMessage(opponent, "&6&l" + opponent.getName() + " &a&lwins the duel! &7(" + quitter.getName() + " left the game)");
            
            // End duel with opponent as winner
            plugin.getDuelManager().endDuel(duel, opponent);
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Duel duel = plugin.getDuelManager().getDuel(player);
        
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            Kit kit = duel.getKit();
            if (!kit.getRule("block-break")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Duel duel = plugin.getDuelManager().getDuel(player);
        
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            Kit kit = duel.getKit();
            if (!kit.getRule("block-place")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        Duel duel = plugin.getDuelManager().getDuel(player);
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            Kit kit = duel.getKit();
            if (!kit.getRule("natural-health-regen") && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        Duel duel = plugin.getDuelManager().getDuel(player);
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            Kit kit = duel.getKit();
            if (!kit.getRule("hunger-loss")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Duel duel = plugin.getDuelManager().getDuel(player);
        
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            Kit kit = duel.getKit();
            if (!kit.getRule("item-drop")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Duel duel = plugin.getDuelManager().getDuel(player);
        
        if (duel != null && duel.getState() == Duel.DuelState.IN_PROGRESS) {
            Kit kit = duel.getKit();
            if (!kit.getRule("item-pickup")) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) {
            return;
        }
        
        Duel duel = plugin.getDuelManager().getDuel(victim);
        if (duel != null && duel.hasPlayer(attacker)) {
            // Cancel damage during countdown and round delays
            if (duel.getState() != Duel.DuelState.IN_PROGRESS) {
                event.setCancelled(true);
            }
        }
    }
}