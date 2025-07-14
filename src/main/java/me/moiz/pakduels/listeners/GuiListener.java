package me.moiz.pakduels.listeners;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.guis.ArenaEditorGui;
import me.moiz.pakduels.guis.ArenaListGui;
import me.moiz.pakduels.guis.KitEditorGui;
import me.moiz.pakduels.guis.KitSelectorGui;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class GuiListener implements Listener {
    private final PakDuelsPlugin plugin;
    
    public GuiListener(PakDuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();
        
        if (title.equals("Arena Manager")) {
            event.setCancelled(true);
            handleArenaListClick(player, event.getSlot());
        } else if (title.startsWith("Arena Editor: ")) {
            event.setCancelled(true);
            ArenaEditorGui gui = plugin.getGuiManager().getArenaEditorGui(player);
            if (gui != null) {
                gui.handleClick(event.getSlot());
            }
        } else if (title.startsWith("Kit Editor: ")) {
            event.setCancelled(true);
            KitEditorGui gui = plugin.getGuiManager().getKitEditorGui(player);
            if (gui != null) {
                gui.handleClick(event.getSlot());
            }
        } else if (title.startsWith("Kit Selector: ")) {
            event.setCancelled(true);
            KitSelectorGui gui = plugin.getGuiManager().getKitSelectorGui(player);
            if (gui != null) {
                gui.handleClick(event.getSlot());
            }
        }
    }
    
    private void handleArenaListClick(Player player, int slot) {
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        
        // Calculate arena index based on slot position
        int row = slot / 9;
        int col = slot % 9;
        
        if (row < 1 || row > 4 || col < 1 || col > 7) {
            return; // Invalid slot
        }
        
        int arenaIndex = (row - 1) * 7 + (col - 1);
        List<Arena> arenas = plugin.getArenaManager().getAllArenas().stream().toList();
        
        if (arenaIndex >= 0 && arenaIndex < arenas.size()) {
            Arena arena = arenas.get(arenaIndex);
            player.closeInventory();
            plugin.getGuiManager().openArenaEditorGUI(player, arena);
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ArenaEditorGui gui = plugin.getGuiManager().getArenaEditorGui(player);
        
        if (gui != null && gui.getEditMode() != ArenaEditorGui.EditMode.NONE) {
            // Check for shift + left click in air or on block
            if (player.isSneaking() && (event.getAction() == Action.LEFT_CLICK_AIR || 
                event.getAction() == Action.LEFT_CLICK_BLOCK || 
                event.getAction() == Action.RIGHT_CLICK_AIR || 
                event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                event.setCancelled(true);
                
                Arena arena = gui.getArena();
                org.bukkit.Location playerLoc = player.getLocation();
                
                switch (gui.getEditMode()) {
                    case POSITION_1:
                        arena.setPosition1(playerLoc.clone());
                        MessageUtils.sendRawMessage(player, "&aArena Position 1 set!");
                        break;
                    case POSITION_2:
                        arena.setPosition2(playerLoc.clone());
                        MessageUtils.sendRawMessage(player, "&aArena Position 2 set!");
                        break;
                    case SPAWN_1:
                        arena.setSpawnPoint1(playerLoc.clone());
                        MessageUtils.sendRawMessage(player, "&aSpawn Point 1 set!");
                        break;
                    case SPAWN_2:
                        arena.setSpawnPoint2(playerLoc.clone());
                        MessageUtils.sendRawMessage(player, "&aSpawn Point 2 set!");
                        break;
                }
                
                gui.setEditMode(ArenaEditorGui.EditMode.NONE);
                plugin.getArenaManager().saveArena(arena);
                gui.refresh();
                gui.open();
            }
        }
    }
}