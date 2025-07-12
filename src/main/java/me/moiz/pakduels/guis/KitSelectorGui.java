package me.moiz.pakduels.guis;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class KitSelectorGui {
    private final PakDuelsPlugin plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory inventory;
    
    public KitSelectorGui(PakDuelsPlugin plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;
        this.inventory = Bukkit.createInventory(null, 54, "Kit Selector: " + arena.getName());
        
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
        
        // Allow All Kits button
        ItemStack allowAll = new ItemStack(arena.getAllowedKits().isEmpty() ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta allowAllMeta = allowAll.getItemMeta();
        allowAllMeta.setDisplayName("§a§lAllow All Kits");
        allowAllMeta.setLore(Arrays.asList(
            "§7Allow all kits to use this arena",
            "§7Status: " + (arena.getAllowedKits().isEmpty() ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        allowAll.setItemMeta(allowAllMeta);
        inventory.setItem(10, allowAll);
        
        // Add individual kits
        List<Kit> kits = plugin.getKitManager().getAllKits().stream().toList();
        int slot = 19; // Start from slot 19 (third row, second column)
        
        for (int i = 0; i < kits.size() && slot < 44; i++) {
            Kit kit = kits.get(i);
            
            // Skip border slots
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot++;
                if (slot % 9 == 0 || slot % 9 == 8) {
                    slot++;
                }
            }
            
            boolean isAllowed = arena.isKitAllowed(kit.getName());
            ItemStack kitItem = new ItemStack(isAllowed ? Material.EMERALD : Material.RED_CONCRETE);
            ItemMeta kitMeta = kitItem.getItemMeta();
            kitMeta.setDisplayName("§6§l" + kit.getName());
            kitMeta.setLore(Arrays.asList(
                "§7Status: " + (isAllowed ? "§aAllowed" : "§cNot Allowed"),
                "",
                "§e§lClick to toggle!"
            ));
            kitItem.setItemMeta(kitMeta);
            
            inventory.setItem(slot, kitItem);
            slot++;
        }
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§7§lBack");
        backMeta.setLore(Arrays.asList(
            "§7Return to arena editor",
            "",
            "§e§lClick to go back!"
        ));
        back.setItemMeta(backMeta);
        inventory.setItem(49, back);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void refresh() {
        setupGui();
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public void handleClick(int slot) {
        if (slot == 10) {
            // Allow All Kits toggle
            if (arena.getAllowedKits().isEmpty()) {
                // Currently allowing all, restrict to specific kits
                for (Kit kit : plugin.getKitManager().getAllKits()) {
                    arena.addAllowedKit(kit.getName());
                }
                MessageUtils.sendMessage(player, "&cNow restricting to specific kits!");
            } else {
                // Currently restricted, allow all
                arena.getAllowedKits().clear();
                MessageUtils.sendMessage(player, "&aNow allowing all kits!");
            }
            plugin.getArenaManager().saveArena(arena);
            refresh();
        } else if (slot == 49) {
            // Back button
            plugin.getGuiManager().removeKitSelectorGui(player);
            plugin.getGuiManager().openArenaEditorGUI(player, arena);
        } else {
            // Kit selection
            int row = slot / 9;
            int col = slot % 9;
            
            if (row >= 2 && row <= 4 && col >= 1 && col <= 7) {
                int kitIndex = (row - 2) * 7 + (col - 1);
                List<Kit> kits = plugin.getKitManager().getAllKits().stream().toList();
                
                if (kitIndex >= 0 && kitIndex < kits.size()) {
                    Kit kit = kits.get(kitIndex);
                    
                    if (arena.getAllowedKits().contains(kit.getName())) {
                        arena.removeAllowedKit(kit.getName());
                        MessageUtils.sendMessage(player, "&cKit &f" + kit.getName() + " &cis no longer allowed!");
                    } else {
                        arena.addAllowedKit(kit.getName());
                        MessageUtils.sendMessage(player, "&aKit &f" + kit.getName() + " &ais now allowed!");
                    }
                    
                    plugin.getArenaManager().saveArena(arena);
                    refresh();
                }
            }
        }
    }
}