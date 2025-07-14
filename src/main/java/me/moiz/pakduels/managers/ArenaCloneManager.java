package me.moiz.pakduels.guis;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.ArrayList;

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
        ItemStack pos1 = new ItemStack(arena.getPosition1() != null ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta pos1Meta = pos1.getItemMeta();
        pos1Meta.setDisplayName("§c§lPosition 1");
        List<String> pos1Lore = new ArrayList<>(Arrays.asList(
            "§7Set the first corner of the arena",
            "§7Status: " + (arena.getPosition1() != null ? "§aSet" : "§cNot set")
        ));
        if (arena.getPosition1() != null) {
            Location pos1 = arena.getPosition1();
            pos1Lore.add("§7Coordinates:");
            pos1Lore.add("§fX: " + pos1.getBlockX());
            pos1Lore.add("§fY: " + pos1.getBlockY());
            pos1Lore.add("§fZ: " + pos1.getBlockZ());
        }
        pos1Lore.add("");
        pos1Lore.add("§e§lClick to set!");
        pos1Meta.setLore(pos1Lore);
        pos1.setItemMeta(pos1Meta);
        inventory.setItem(19, pos1);
        
        // Position 2
        ItemStack pos2 = new ItemStack(arena.getPosition2() != null ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta pos2Meta = pos2.getItemMeta();
        pos2Meta.setDisplayName("§c§lPosition 2");
        List<String> pos2Lore = new ArrayList<>(Arrays.asList(
            "§7Set the second corner of the arena",
            "§7Status: " + (arena.getPosition2() != null ? "§aSet" : "§cNot set")
        ));
        if (arena.getPosition2() != null) {
            Location pos2 = arena.getPosition2();
            pos2Lore.add("§7Coordinates:");
            pos2Lore.add("§fX: " + pos2.getBlockX());
            pos2Lore.add("§fY: " + pos2.getBlockY());
            pos2Lore.add("§fZ: " + pos2.getBlockZ());
        }
        pos2Lore.add("");
        pos2Lore.add("§e§lClick to set!");
        pos2Meta.setLore(pos2Lore);
        pos2.setItemMeta(pos2Meta);
        inventory.setItem(21, pos2);
        
        // Spawn 1
        ItemStack spawn1 = new ItemStack(arena.getSpawnPoint1() != null ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta spawn1Meta = spawn1.getItemMeta();
        spawn1Meta.setDisplayName("§a§lSpawn Point 1");
        List<String> spawn1Lore = new ArrayList<>(Arrays.asList(
            "§7Set the first player spawn point",
            "§7Status: " + (arena.getSpawnPoint1() != null ? "§aSet" : "§cNot set")
        ));
        if (arena.getSpawnPoint1() != null) {
            Location spawn1 = arena.getSpawnPoint1();
            spawn1Lore.add("§7Coordinates:");
            spawn1Lore.add("§fX: " + spawn1.getBlockX());
            spawn1Lore.add("§fY: " + spawn1.getBlockY());
            spawn1Lore.add("§fZ: " + spawn1.getBlockZ());
            spawn1Lore.add("§fYaw: " + String.format("%.1f", spawn1.getYaw()));
            spawn1Lore.add("§fPitch: " + String.format("%.1f", spawn1.getPitch()));
        }
        spawn1Lore.add("");
        spawn1Lore.add("§e§lClick to set!");
        spawn1Meta.setLore(spawn1Lore);
        spawn1.setItemMeta(spawn1Meta);
        inventory.setItem(23, spawn1);
        
        // Spawn 2
        ItemStack spawn2 = new ItemStack(arena.getSpawnPoint2() != null ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta spawn2Meta = spawn2.getItemMeta();
        spawn2Meta.setDisplayName("§a§lSpawn Point 2");
        List<String> spawn2Lore = new ArrayList<>(Arrays.asList(
            "§7Set the second player spawn point",
            "§7Status: " + (arena.getSpawnPoint2() != null ? "§aSet" : "§cNot set")
        ));
        if (arena.getSpawnPoint2() != null) {
            Location spawn2 = arena.getSpawnPoint2();
            spawn2Lore.add("§7Coordinates:");
            spawn2Lore.add("§fX: " + spawn2.getBlockX());
        spawn2.setItemMeta(spawn2Meta);
        inventory.setItem(25, spawn2);
        
        ItemStack allowedKits = new ItemStack(Material.BOOK);
        ItemMeta allowedKitsMeta = allowedKits.getItemMeta();
        allowedKitsMeta.setDisplayName("§b§lAllowed Kits");
        allowedKitsMeta.setLore(Arrays.asList(
            "§7Manage which kits can use this arena",
            "§7Current: " + (arena.getAllowedKits().isEmpty() ? "§aAll kits" : "§f" + String.join(", ", arena.getAllowedKits())),
            "",
            "§e§lClick to manage!"
        ));
        allowedKits.setItemMeta(allowedKitsMeta);
        inventory.setItem(29, allowedKits);
        
        // Regeneration toggle
        ItemStack regen = new ItemStack(arena.isRegenerationEnabled() ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta regenMeta = regen.getItemMeta();
        regenMeta.setDisplayName("§d§lRegeneration");
        regenMeta.setLore(Arrays.asList(
            "§7Toggle arena regeneration",
            "§7Status: " + (arena.isRegenerationEnabled() ? "§aEnabled" : "§cDisabled"),
            "",
            "§e§lClick to toggle!"
        ));
        regen.setItemMeta(regenMeta);
        inventory.setItem(31, regen);
        
        // Set Center button
        ItemStack setCenter = new ItemStack(arena.getCenter() != null ? Material.LIME_DYE : Material.RED_DYE);
        ItemMeta setCenterMeta = setCenter.getItemMeta();
        setCenterMeta.setDisplayName("§e§lSet Center");
        setCenterMeta.setLore(Arrays.asList(
            "§7Set the center point for cloning",
            "§7Status: " + (arena.getCenter() != null ? "§aSet" : "§cNot set"),
            "",
            "§e§lClick to set!"
        ));
        setCenter.setItemMeta(setCenterMeta);
        inventory.setItem(33, setCenter);
        
        // Clone Arena button
        ItemStack cloneArena = new ItemStack(Material.STRUCTURE_BLOCK);
        ItemMeta cloneArenaMeta = cloneArena.getItemMeta();
        cloneArenaMeta.setDisplayName("§b§lClone Arena");
        cloneArenaMeta.setLore(Arrays.asList(
            "§7Clone this arena to your location",
            "§7Requires center point to be set",
            "",
            "§e§lClick to clone!"
        ));
        cloneArena.setItemMeta(cloneArenaMeta);
        inventory.setItem(34, cloneArena);
        
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
        
        // Delete button
        ItemStack delete = new ItemStack(Material.RED_CONCRETE);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.setDisplayName("§c§lDelete Arena");
        deleteMeta.setLore(Arrays.asList(
            "§7Delete this arena permanently",
            "§c§lWARNING: This cannot be undone!",
            "",
            "§e§lClick to delete!"
        ));
        delete.setItemMeta(deleteMeta);
        inventory.setItem(46, delete);
        
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
    
    public EditMode getEditMode() {
        return editMode;
    }
    
    public void setEditMode(EditMode editMode) {
        this.editMode = editMode;
    }
    
    public void handleClick(int slot) {
        switch (slot) {
            case 19: // Position 1
                setEditMode(EditMode.POSITION_1);
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aClick a block to set Position 1!");
                break;
            case 21: // Position 2
                setEditMode(EditMode.POSITION_2);
                player.closeInventory();
                MessageUtils.sendMessage(player, "&aClick a block to set Position 2!");
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
            case 29: // Allowed Kits
                plugin.getGuiManager().openKitSelectorGUI(player, arena);
                break;
            case 31: // Regeneration toggle
                arena.setRegenerationEnabled(!arena.isRegenerationEnabled());
                MessageUtils.sendRawMessage(player, "&aRegeneration " + (arena.isRegenerationEnabled() ? "enabled" : "disabled") + "!");
                
                // Auto-save schematic when regeneration is enabled
                if (arena.isRegenerationEnabled() && arena.isComplete()) {
                    MessageUtils.sendRawMessage(player, "&eSaving arena schematic...");
                    plugin.getArenaCloneManager().saveArenaSchematic(arena).whenComplete((success, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().severe("Exception during schematic save: " + throwable.getMessage());
                            throwable.printStackTrace();
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                MessageUtils.sendRawMessage(player, "&cError saving schematic: " + throwable.getMessage());
                            });
                            return;
                        }
                        
                        if (success) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                MessageUtils.sendRawMessage(player, "&aSchematic saved successfully!");
                            });
                        } else {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                MessageUtils.sendRawMessage(player, "&cFailed to save schematic!");
                            });
                        }
                    });
                }
                
                plugin.getArenaManager().saveArena(arena);
                refresh();
                break;
            case 33: // Set Center
                arena.setCenter(player.getLocation().clone());
                MessageUtils.sendRawMessage(player, "&aArena center set!");
                plugin.getArenaManager().saveArena(arena);
                refresh();
                break;
            case 34: // Clone Arena
                cloneArena();
                break;
            case 45: // Save
                plugin.getArenaManager().saveArena(arena);
                MessageUtils.sendRawMessage(player, "&aArena saved successfully!");
                player.closeInventory();
                break;
            case 46: // Delete
                plugin.getArenaManager().removeArena(arena.getName());
                plugin.getGuiManager().removeArenaEditorGui(player);
                MessageUtils.sendRawMessage(player, "&cArena deleted successfully!");
                player.closeInventory();
                break;
            case 53: // Back
                plugin.getGuiManager().removeArenaEditorGui(player);
                plugin.getGuiManager().openArenaListGUI(player);
                break;
        }
    }
    
    private void cloneArena() {
        if (arena.getCenter() == null) {
            MessageUtils.sendRawMessage(player, "&cYou must set the arena center first!");
            return;
        }
        
        if (!arena.isComplete()) {
            MessageUtils.sendRawMessage(player, "&cOriginal arena must be complete before cloning!");
            return;
        }
        
        // Generate unique clone name
        String baseName = arena.getName() + "_clone";
        String cloneName = baseName;
        int counter = 1;
        while (plugin.getArenaManager().hasArena(cloneName)) {
            cloneName = baseName + "_" + counter;
            counter++;
        }
        
        Location originalCenter = arena.getCenter();
        Location newCenter = player.getLocation().clone();
        
        // Calculate offsets from original center
        Location pos1Offset = arena.getPosition1().clone().subtract(originalCenter);
        Location pos2Offset = arena.getPosition2().clone().subtract(originalCenter);
        Location spawn1Offset = arena.getSpawnPoint1().clone().subtract(originalCenter);
        Location spawn2Offset = arena.getSpawnPoint2().clone().subtract(originalCenter);
        
        // Apply offsets to new center
        Location newPos1 = newCenter.clone().add(pos1Offset);
        Location newPos2 = newCenter.clone().add(pos2Offset);
        Location newSpawn1 = newCenter.clone().add(spawn1Offset);
        Location newSpawn2 = newCenter.clone().add(spawn2Offset);
        
        // Create new arena
        Arena clonedArena = new Arena(cloneName, newPos1, newPos2, newSpawn1, newSpawn2);
        clonedArena.setCenter(newCenter);
        clonedArena.setAllowedKits(new ArrayList<>(arena.getAllowedKits()));
        clonedArena.setRegenerationEnabled(arena.isRegenerationEnabled());
        
        // Save the cloned arena
        plugin.getArenaManager().addArena(clonedArena);
        
        // Handle schematic cloning with FAWE
        plugin.getArenaCloneManager().cloneArenaSchematic(arena, clonedArena, player);
        
        MessageUtils.sendRawMessage(player, "&aArena cloned successfully as &f" + cloneName + "&a!");
        player.closeInventory();
    }
}