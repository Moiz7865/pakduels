package me.moiz.pakduels.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.CompletableFuture;

public class ArenaCloneManager {
    private final PakDuelsPlugin plugin;
    private final File schematicsFolder;
    
    public ArenaCloneManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        
        // Ensure directory exists with proper permissions
        if (!schematicsFolder.exists()) {
            boolean created = schematicsFolder.mkdirs();
            plugin.getLogger().info("Created schematics folder: " + created);
        }
        
        plugin.getLogger().info("Schematics folder path: " + schematicsFolder.getAbsolutePath());
        plugin.getLogger().info("Schematics folder writable: " + schematicsFolder.canWrite());
    }
    
    public CompletableFuture<Boolean> saveArenaSchematic(Arena arena) {
        if (!arena.isComplete()) {
            plugin.getLogger().warning("Cannot save schematic for incomplete arena: " + arena.getName());
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.getLogger().info("Starting schematic save for arena: " + arena.getName());
                
                // Check if world exists
                if (arena.getPosition1().getWorld() == null) {
                    plugin.getLogger().severe("Arena world is null for: " + arena.getName());
                    return false;
                }
                
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(arena.getPosition1().getWorld());
                plugin.getLogger().info("World adapted successfully: " + world.getName());
                
                // Use raw positions (no min/max) to preserve exact structure
                BlockVector3 pos1 = BlockVector3.at(
                    arena.getPosition1().getBlockX(),
                    arena.getPosition1().getBlockY(),
                    arena.getPosition1().getBlockZ()
                );
                
                BlockVector3 pos2 = BlockVector3.at(
                    arena.getPosition2().getBlockX(),
                    arena.getPosition2().getBlockY(),
                    arena.getPosition2().getBlockZ()
                );
                
                plugin.getLogger().info("Positions - Pos1: " + pos1 + ", Pos2: " + pos2);
                
                CuboidRegion region = new CuboidRegion(world, pos1, pos2);
                plugin.getLogger().info("Region created with volume: " + region.getVolume());
                
                File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                plugin.getLogger().info("Schematic file path: " + schematicFile.getAbsolutePath());
                
                // Ensure parent directory exists
                if (!schematicFile.getParentFile().exists()) {
                    schematicFile.getParentFile().mkdirs();
                }
                
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    plugin.getLogger().info("EditSession created successfully");
                    
                    Clipboard clipboard = editSession.lazyCopy(region);
                    plugin.getLogger().info("Clipboard created with dimensions: " + clipboard.getDimensions());
                    
                    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                    if (format == null) {
                        plugin.getLogger().severe("Could not find clipboard format for .schem file");
                        return false;
                    }
                    
                    plugin.getLogger().info("Using format: " + format.getName());
                    
                    try (FileOutputStream fos = new FileOutputStream(schematicFile);
                         ClipboardWriter writer = format.getWriter(fos)) {
                        
                        writer.write(clipboard);
                        plugin.getLogger().info("Schematic written to file successfully");
                    }
                    
                    // Verify file was created
                    if (schematicFile.exists() && schematicFile.length() > 0) {
                        plugin.getLogger().info("Schematic file verified - Size: " + schematicFile.length() + " bytes");
                        return true;
                    } else {
                        plugin.getLogger().severe("Schematic file was not created or is empty");
                        return false;
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save schematic for arena: " + arena.getName());
                plugin.getLogger().severe("Error details: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    public void cloneArenaSchematic(Arena originalArena, Arena clonedArena, Player player) {
        File schematicFile = new File(schematicsFolder, originalArena.getName() + ".schem");
        
        if (!schematicFile.exists()) {
            MessageUtils.sendRawMessage(player, "&eCreating schematic for arena...");
            saveArenaSchematic(originalArena).thenAccept(success -> {
                if (success) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        cloneArenaSchematic(originalArena, clonedArena, player);
                    }, 40L);
                } else {
                    MessageUtils.sendRawMessage(player, "&cFailed to create schematic!");
                }
            });
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                plugin.getLogger().info("Starting arena clone for: " + originalArena.getName());
                
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(clonedArena.getCenter().getWorld());
                
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtils.sendRawMessage(player, "&cFailed to load schematic format!");
                    });
                    return;
                }
                
                Clipboard clipboard;
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = format.getReader(fis)) {
                    clipboard = reader.read();
                    plugin.getLogger().info("Clipboard loaded successfully");
                }
                
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(
                            clonedArena.getCenter().getBlockX(),
                            clonedArena.getCenter().getBlockY(),
                            clonedArena.getCenter().getBlockZ()
                        ))
                        .build();
                    
                    Operations.complete(operation);
                    plugin.getLogger().info("Arena cloned successfully");
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtils.sendRawMessage(player, "&aArena schematic pasted successfully!");
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to clone arena schematic: " + e.getMessage());
                e.printStackTrace();
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendRawMessage(player, "&cFailed to paste arena schematic! Check console for details.");
                });
            }
        });
    }
    
    public CompletableFuture<Boolean> regenerateArena(Arena arena) {
        File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
        
        if (!schematicFile.exists()) {
            plugin.getLogger().warning("No schematic found for arena: " + arena.getName() + ", creating one...");
            return saveArenaSchematic(arena).thenCompose(success -> {
                if (success) {
                    return regenerateArena(arena);
                } else {
                    return CompletableFuture.completedFuture(false);
                }
            });
        }
        
        if (arena.getCenter() == null) {
            plugin.getLogger().warning("No center set for arena: " + arena.getName());
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(arena.getCenter().getWorld());
                
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    plugin.getLogger().warning("Failed to load schematic format for arena: " + arena.getName());
                    return false;
                }
                
                Clipboard clipboard;
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = format.getReader(fis)) {
                    clipboard = reader.read();
                }
                
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(
                            arena.getCenter().getBlockX(),
                            arena.getCenter().getBlockY(),
                            arena.getCenter().getBlockZ()
                        ))
                        .build();
                    
                    Operations.complete(operation);
                    plugin.getLogger().info("Regenerated arena: " + arena.getName());
                    return true;
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to regenerate arena: " + arena.getName());
                plugin.getLogger().severe("Error: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        });
    }
}