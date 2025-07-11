package me.moiz.pakduels.guis;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ArenaListGui {
    private final PakDuelsPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    
    public ArenaListGui(PakDuelsPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "Arena Manager");
        
        setupGui();
    }
    
    private void setupGui() {
        // Fill background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.setDisplayName(" ");
        background.setItemMeta(backgroundMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, background);
        }
        
        // Add arenas
        List<Arena> arenas = plugin.getArenaManager().getAllArenas().stream().toList();
        int slot = 10; // Start from slot 10 (second row, second column)
        
        for (int i = 0; i < arenas.size() && slot < 44; i++) {
            Arena arena = arenas.get(i);
            
            // Skip border slots
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                }
            }
            
            ItemStack arenaItem = new ItemStack(arena.isComplete() ? Material.EMERALD : Material.RED_CONCRETE);
            ItemMeta arenaMeta = arenaItem.getItemMeta();
            arenaMeta.setDisplayName("§6§l" + arena.getName());
            arenaMeta.setLore(Arrays.asList(
                "§7Status: " + (arena.isComplete() ? "§aComplete" : "§cIncomplete"),
                "§7Reserved: " + (arena.isReserved() ? "§cYes" : "§aNo"),
                "§7Regeneration: " + (arena.isRegenerationEnabled() ? "§aEnabled" : "§cDisabled"),
                "§7Allowed Kits: §f" + (arena.getAllowedKits().isEmpty() ? "All" : String.join(", ", arena.getAllowedKits())),
                "",
                "§e§lClick to edit!"
            ));
            arenaItem.setItemMeta(arenaMeta);
            
            inventory.setItem(slot, arenaItem);
            slot++;
        }
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§c§lClose");
        closeMeta.setLore(Arrays.asList(
            "§7Close this menu",
            "",
            "§e§lClick to close!"
        ));
        close.setItemMeta(closeMeta);
        inventory.setItem(49, close);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void handleClick(int slot) {
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
}