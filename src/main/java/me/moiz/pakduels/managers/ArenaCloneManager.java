package me.moiz.pakduels.managers;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ArenaCloneManager {
    private final PakDuelsPlugin plugin;
    private final File schematicsFolder;
    
    public ArenaCloneManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        
        // Create schematics folder if it doesn't exist
        if (!schematicsFolder.exists()) {
            boolean created = schematicsFolder.mkdirs();
            plugin.getLogger().info("Schematics folder created: " + created);
        }
    }
    
    public CompletableFuture<Boolean> saveArenaSchematic(Arena arena) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.getLogger().info("Starting schematic save for arena: " + arena.getName());
                
                // Validate arena
                if (!arena.isComplete()) {
                    plugin.getLogger().warning("Arena is not complete: " + arena.getName());
                    return false;
                }
                
                Location pos1 = arena.getPosition1();
                Location pos2 = arena.getPosition2();
                
                // Validate world
                if (!pos1.getWorld().equals(pos2.getWorld())) {
                    plugin.getLogger().warning("Arena positions are in different worlds");
                    return false;
                }
                
                // Adapt world
                World world = BukkitAdapter.adapt(pos1.getWorld());
                plugin.getLogger().info("World adapted successfully: " + world.getName());
                
                // Create positions using raw coordinates (no min/max)
                BlockVector3 position1 = BlockVector3.at(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ());
                BlockVector3 position2 = BlockVector3.at(pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ());
                
                plugin.getLogger().info("Positions - Pos1: " + position1 + ", Pos2: " + position2);
                
                // Create region
                CuboidRegion region = new CuboidRegion(world, position1, position2);
                long volume = region.getVolume();
                plugin.getLogger().info("Region created with volume: " + volume);
                
                if (volume > 10000000) { // 10 million blocks limit
                    plugin.getLogger().warning("Arena too large: " + volume + " blocks");
                    return false;
                }
                
                // Create schematic file
                File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                plugin.getLogger().info("Schematic file path: " + schematicFile.getAbsolutePath());
                
                // Create parent directories
                if (!schematicFile.getParentFile().exists()) {
                    schematicFile.getParentFile().mkdirs();
                }
                
                // Create EditSession
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    plugin.getLogger().info("EditSession created successfully");
                    
                    // Create clipboard
                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                    plugin.getLogger().info("Clipboard created with dimensions: " + clipboard.getDimensions());
                    
                    // Copy to clipboard
                    ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
                    Operations.complete(copy);
                    plugin.getLogger().info("Blocks copied to clipboard successfully");
                    
                    // Get format - try multiple approaches
                    ClipboardFormat format = null;
                    
                    // Try by alias first
                    format = ClipboardFormats.findByAlias("sponge");
                    if (format == null) {
                        // Try by extension
                        format = ClipboardFormats.findByExtension("schem");
                    }
                    if (format == null) {
                        // Use built-in format
                        format = BuiltInClipboardFormat.SPONGE_SCHEMATIC;
                    }
                    
                    if (format == null) {
                        plugin.getLogger().severe("Could not find clipboard format for .schem file");
                        return false;
                    }
                    
                    plugin.getLogger().info("Using clipboard format: " + format.getName());
                    
                    // Write to file
                    try (FileOutputStream fos = new FileOutputStream(schematicFile);
                         ClipboardWriter writer = format.getWriter(fos)) {
                        
                        writer.write(clipboard);
                        plugin.getLogger().info("Schematic written to file successfully");
                    }
                    
                    // Verify file was created
                    if (schematicFile.exists() && schematicFile.length() > 0) {
                        plugin.getLogger().info("Schematic file created successfully. Size: " + schematicFile.length() + " bytes");
                        return true;
                    } else {
                        plugin.getLogger().severe("Schematic file was not created or is empty");
                        return false;
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Exception during schematic save: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    public void cloneArenaSchematic(Arena originalArena, Arena clonedArena, Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                File schematicFile = new File(schematicsFolder, originalArena.getName() + ".schem");
                
                // Create schematic if it doesn't exist
                if (!schematicFile.exists()) {
                    plugin.getLogger().info("Schematic doesn't exist, creating it first...");
                    Boolean saved = saveArenaSchematic(originalArena).get();
                    if (!saved) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            MessageUtils.sendRawMessage(player, "&cFailed to create schematic for cloning!");
                        });
                        return;
                    }
                }
                
                // Load and paste schematic
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    format = BuiltInClipboardFormat.SPONGE_SCHEMATIC;
                }
                
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = format.getReader(fis)) {
                    
                    Clipboard clipboard = reader.read();
                    World world = BukkitAdapter.adapt(clonedArena.getCenter().getWorld());
                    
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                        Operation operation = clipboard
                                .createPaste(editSession)
                                .to(BlockVector3.at(
                                        clonedArena.getCenter().getBlockX(),
                                        clonedArena.getCenter().getBlockY(),
                                        clonedArena.getCenter().getBlockZ()))
                                .build();
                        
                        Operations.complete(operation);
                        
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            MessageUtils.sendRawMessage(player, "&aSchematic pasted successfully!");
                        });
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Exception during schematic cloning: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendRawMessage(player, "&cFailed to clone arena schematic!");
                });
            }
        });
    }
    
    public CompletableFuture<Boolean> regenerateArena(Arena arena) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                
                if (!schematicFile.exists()) {
                    plugin.getLogger().warning("No schematic found for arena: " + arena.getName());
                    return false;
                }
                
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    format = BuiltInClipboardFormat.SPONGE_SCHEMATIC;
                }
                
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = format.getReader(fis)) {
                    
                    Clipboard clipboard = reader.read();
                    World world = BukkitAdapter.adapt(arena.getCenter().getWorld());
                    
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                        Operation operation = clipboard
                                .createPaste(editSession)
                                .to(BlockVector3.at(
                                        arena.getCenter().getBlockX(),
                                        arena.getCenter().getBlockY(),
                                        arena.getCenter().getBlockZ()))
                                .build();
                        
                        Operations.complete(operation);
                        plugin.getLogger().info("Arena regenerated: " + arena.getName());
                        return true;
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Exception during arena regeneration: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
}