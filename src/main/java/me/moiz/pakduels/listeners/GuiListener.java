package me.moiz.pakduels.listeners;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.guis.ArenaEditorGui;
import me.moiz.pakduels.guis.ArenaListGui;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

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
            
            // Find the ArenaListGui (this is a simplified approach)
            ArenaListGui gui = new ArenaListGui(plugin, player);
            if (inventory.equals(gui.getInventory())) {
                gui.handleClick(event.getSlot());
            }
        } else if (title.startsWith("Arena Editor: ")) {
            event.setCancelled(true);
            
            ArenaEditorGui gui = plugin.getGuiManager().getArenaEditorGui(player);
            if (gui != null && inventory.equals(gui.getInventory())) {
                gui.handleClick(event.getSlot());
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ArenaEditorGui gui = plugin.getGuiManager().getArenaEditorGui(player);
        
        if (gui != null && gui.getEditMode() != ArenaEditorGui.EditMode.NONE) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                
                Arena arena = gui.getArena();
                
                switch (gui.getEditMode()) {
                    case POSITION_1:
                        arena.setPosition1(event.getClickedBlock().getLocation());
                        MessageUtils.sendMessage(player, "&aArena Position 1 set!");
                        break;
                    case POSITION_2:
                        arena.setPosition2(event.getClickedBlock().getLocation());
                        MessageUtils.sendMessage(player, "&aArena Position 2 set!");
                        break;
                    case SPAWN_1:
                        arena.setSpawnPoint1(event.getClickedBlock().getLocation().add(0.5, 1, 0.5));
                        MessageUtils.sendMessage(player, "&aSpawn Point 1 set!");
                        break;
                    case SPAWN_2:
                        arena.setSpawnPoint2(event.getClickedBlock().getLocation().add(0.5, 1, 0.5));
                        MessageUtils.sendMessage(player, "&aSpawn Point 2 set!");
                        break;
                }
                
                gui.setEditMode(ArenaEditorGui.EditMode.NONE);
                gui.refresh();
                gui.open();
            }
        }
    }
}