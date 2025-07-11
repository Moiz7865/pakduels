package me.moiz.pakduels.guis;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ArenaEditorGui {
    private final PakDuelsPlugin plugin;
    private final Player player;
    private final Arena arena;
    private final Inventory inventory;
    private EditMode editMode;
    
    public enum EditMode {
        NONE,
        POSITION_1,
        POSITION_2,
        SPAWN_1,
        SPAWN_2
    }
    
    public ArenaEditorGui(PakDuelsPlugin plugin, Player player, Arena arena) {
        this.plugin = plugin;
        this.player = player;
        this.arena = arena;
        this.inventory = Bukkit.createInventory(null, 54, "Arena Editor: " + arena.getName());
        this.editMode = EditMode.NONE;
        
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
        
        // Position 1
        ItemStack pos1 = new ItemStack(Material.RED_WOOL);
        ItemMeta pos1Meta = pos1.getItemMeta();
        pos1Meta.setDisplayName("§c§lArena Position 1");
        pos1Meta.setLore(Arrays.asList(
            "§7Set the first corner of the arena",
            arena.getPosition1() != null ? "§aSet: §f" + locationToString(arena.getPosition1()) : "§cNot set",
            "",
            "§e§lClick to set!"
        ));
        pos1.setItemMeta(pos1Meta);
        inventory.setItem(19, pos1);
        
        // Position 2
        ItemStack pos2 = new ItemStack(Material.BLUE_WOOL);
        ItemMeta pos2Meta = pos2.getItemMeta();
        pos2Meta.setDisplayName("§9§lArena Position 2");
        pos2Meta.setLore(Arrays.asList(
            "§7Set the second corner of the arena",
            arena.getPosition2() != null ? "§aSet: §f" + locationToString(arena.getPosition2()) : "§cNot set",
            "",
            "§e§lClick to set!"
        ));
        pos2.setItemMeta(pos2Meta);
        inventory.setItem(21, pos2);
        
        // Spawn 1
        ItemStack spawn1 = new ItemStack(Material.GREEN_WOOL);
        ItemMeta spawn1Meta = spawn1.getItemMeta();
        spawn1Meta.setDisplayName("§a§lSpawn Point 1");
        spawn1Meta.setLore(Arrays.asList(
            "§7Set the first player spawn point",
            arena.getSpawnPoint1() != null ? "§aSet: §f" + locationToString(arena.getSpawnPoint1()) : "§cNot set",
            "",
            "§e§lClick to set!"
        ));
        spawn1.setItemMeta(spawn1Meta);
        inventory.setItem(23, spawn1);
        
        // Spawn 2
        ItemStack spawn2 = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta spawn2Meta = spawn2.getItemMeta();
        spawn2Meta.setDisplayName("§e§lSpawn Point 2");
        spawn2Meta.setLore(Arrays.asList(
            "§7Set the second player spawn point",
            arena.getSpawnPoint2() != null ? "§aSet: §f" + locationToString(arena.getSpawnPoint2()) : "§cNot set",
            "",
            "§e§lClick to set!"
        ));
        spawn2.setItemMeta(spawn2Meta);
        inventory.setItem(25, spawn2);
        
        // Regeneration toggle
        ItemStack regen = new ItemStack(arena.isRegenerationEnabled() ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta regenMeta = regen.getItemMeta();
        regenMeta.setDisplayName("§6§lRegeneration");
        regenMeta.setLore(Arrays.asList(
            "§7Toggle arena regeneration",
            "§7Status: " + (arena.isRegenerationEnabled() ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        regen.setItemMeta(regenMeta);
        inventory.setItem(31, regen);
        
        // Kit management
        ItemStack kits = new ItemStack(Material.CHEST);
        ItemMeta kitsMeta = kits.getItemMeta();
        kitsMeta.setDisplayName("§d§lAllowed Kits");
        List<String> lore = Arrays.asList(
            "§7Manage allowed kits for this arena",
            "§7Current: " + (arena.getAllowedKits().isEmpty() ? "§fAll kits" : "§f" + String.join(", ", arena.getAllowedKits())),
            "",
            "§e§lClick to manage!"
        );
        kitsMeta.setLore(lore);
        kits.setItemMeta(kitsMeta);
        inventory.setItem(33, kits);
        
        // Save button
        ItemStack save = new ItemStack(Material.EMERALD);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName("§a§lSave Arena");
        saveMeta.setLore(Arrays.asList(
            "§7Save all changes to this arena",
            "",
            "§e§lClick to save!"
        ));
        save.setItemMeta(saveMeta);
        inventory.setItem(45, save);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§7§lBack");
        backMeta.setLore(Arrays.asList(
            "§7Return to arena list",
            "",
            "§e§lClick to go back!"
        ));
        back.setItemMeta(backMeta);
        inventory.setItem(53, back);
    }
    
    private String locationToString(org.bukkit.Location location) {
        return String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ());
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
    
    public EditMode getEditMode() {
        return editMode;
    }
    
    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public void handleClick(int slot) {
        switch (slot) {
            case 19: // Position 1
                setEditMode(EditMode.POSITION_1);
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aClick a block to set Arena Position 1!");
                break;
            case 21: // Position 2
                setEditMode(EditMode.POSITION_2);
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aClick a block to set Arena Position 2!");
                break;
            case 23: // Spawn 1
                setEditMode(EditMode.SPAWN_1);
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aClick a block to set Spawn Point 1!");
                break;
            case 25: // Spawn 2
                setEditMode(EditMode.SPAWN_2);
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aClick a block to set Spawn Point 2!");
                break;
            case 31: // Regeneration toggle
                arena.setRegenerationEnabled(!arena.isRegenerationEnabled());
                MessageUtils.sendMessage(player, "&aRegeneration " + (arena.isRegenerationEnabled() ? "enabled" : "disabled") + "!");
                refresh();
                break;
            case 33: // Kit management
                MessageUtils.sendMessage(player, "&cKit management not implemented yet!");
                break;
            case 45: // Save
                plugin.getArenaManager().saveArenas();
                MessageUtils.sendMessage(player, "&aArena saved successfully!");
                break;
            case 53: // Back
                player.closeInventory();
                plugin.getGuiManager().openArenaListGUI(player);
                break;
        }
    }
}