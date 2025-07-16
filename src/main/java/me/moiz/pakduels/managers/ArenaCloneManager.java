package me.moiz.pakduels.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
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
import java.util.concurrent.CompletableFuture;

public class ArenaCloneManager {
    private final PakDuelsPlugin plugin;
    private final File schematicsFolder;
    
    public ArenaCloneManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        
        // Create schematics folder if it doesn't exist
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }
    
    public CompletableFuture<Boolean> saveArenaSchematic(Arena arena) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.getLogger().info("Starting schematic save for arena: " + arena.getName());
                
                // Get world
                World world = BukkitAdapter.adapt(arena.getPosition1().getWorld());
                plugin.getLogger().info("World adapted successfully: " + world.getName());
                
                // Create region
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
                
                // Create schematic file
                File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                plugin.getLogger().info("Schematic file path: " + schematicFile.getAbsolutePath());
                
                // Create edit session
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    plugin.getLogger().info("EditSession created successfully");
                    
                    // Create clipboard
                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                    plugin.getLogger().info("Clipboard created with dimensions: " + clipboard.getDimensions());
                    
                    // Copy to clipboard
                    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                        editSession, region, clipboard, region.getMinimumPoint()
                    );
                    Operations.complete(forwardExtentCopy);
                    plugin.getLogger().info("Copy operation completed successfully");
                    
                    // Get the correct format for .schem files
                    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                    if (format == null) {
                        // Fallback to SPONGE_SCHEMATIC format
                        format = ClipboardFormats.findByAlias("schem");
                        if (format == null) {
                            format = ClipboardFormats.findByAlias("sponge");
                        }
                    }
                    
                    if (format == null) {
                        plugin.getLogger().severe("Could not find schematic format for .schem files!");
                        return false;
                    }
                    
                    plugin.getLogger().info("Using clipboard format: " + format.getName());
                    
                    // Save to file
                    try (FileOutputStream fos = new FileOutputStream(schematicFile);
                         ClipboardWriter writer = format.getWriter(fos)) {
                        
                        writer.write(clipboard);
                        plugin.getLogger().info("Schematic saved successfully to: " + schematicFile.getName());
                        return true;
                    }
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save schematic for arena: " + arena.getName());
                e.printStackTrace();
                return false;
            }
        });
    }
    
    public void cloneArenaSchematic(Arena originalArena, Arena clonedArena, Player player) {
        CompletableFuture.runAsync(() -> {
            try {
                File schematicFile = new File(schematicsFolder, originalArena.getName() + ".schem");
                
                if (!schematicFile.exists()) {
                    // Try to save the schematic first
                    plugin.getLogger().info("Schematic doesn't exist, creating it first...");
                    boolean saved = saveArenaSchematic(originalArena).get();
                    if (!saved) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            MessageUtils.sendRawMessage(player, "&cFailed to create schematic for cloning!");
                        });
                        return;
                    }
                }
                
                // Get the correct format for reading
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    format = ClipboardFormats.findByAlias("schem");
                    if (format == null) {
                        format = ClipboardFormats.findByAlias("sponge");
                    }
                }
                
                if (format == null) {
                    plugin.getLogger().severe("Could not find clipboard format for reading .schem file!");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtils.sendRawMessage(player, "&cCould not find clipboard format for .schem file!");
                    });
                    return;
                }
                
                // Load schematic
                Clipboard clipboard;
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = format.getReader(fis)) {
                    
                    clipboard = reader.read();
                    plugin.getLogger().info("Schematic loaded successfully");
                }
                
                // Get target world and location
                World world = BukkitAdapter.adapt(clonedArena.getCenter().getWorld());
                BlockVector3 to = BlockVector3.at(
                    clonedArena.getCenter().getBlockX(),
                    clonedArena.getCenter().getBlockY(),
                    clonedArena.getCenter().getBlockZ()
                );
                
                plugin.getLogger().info("Pasting schematic to: " + to);
                
                // Paste schematic
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    Operation operation = holder
                        .createPaste(editSession)
                        .to(to)
                        .ignoreAirBlocks(false)
                        .build();
                    
                    Operations.complete(operation);
                    plugin.getLogger().info("Schematic pasted successfully");
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtils.sendRawMessage(player, "&aSchematic pasted successfully!");
                    });
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to clone arena schematic: " + e.getMessage());
                e.printStackTrace();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendRawMessage(player, "&cFailed to clone arena schematic: " + e.getMessage());
                });
            }
        });
    }
    
    public void regenerateArena(Arena arena) {
        if (!arena.isRegenerationEnabled()) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                
                if (!schematicFile.exists()) {
                    plugin.getLogger().warning("Schematic file not found for arena regeneration: " + arena.getName());
                    return;
                }
                
                // Get the correct format for reading
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    format = ClipboardFormats.findByAlias("schem");
                    if (format == null) {
                        format = ClipboardFormats.findByAlias("sponge");
                    }
                }
                
                if (format == null) {
                    plugin.getLogger().severe("Could not find clipboard format for reading .schem file during regeneration!");
                    return;
                }
                
                // Load schematic
                Clipboard clipboard;
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = format.getReader(fis)) {
                    
                    clipboard = reader.read();
                }
                
                // Get target world and location
                World world = BukkitAdapter.adapt(arena.getCenter().getWorld());
                BlockVector3 to = BlockVector3.at(
                    arena.getCenter().getBlockX(),
                    arena.getCenter().getBlockY(),
                    arena.getCenter().getBlockZ()
                );
                
                // Paste schematic
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    ClipboardHolder holder = new ClipboardHolder(clipboard);
                    Operation operation = holder
                        .createPaste(editSession)
                        .to(to)
                        .ignoreAirBlocks(false)
                        .build();
                    
                    Operations.complete(operation);
                    plugin.getLogger().info("Arena regenerated successfully: " + arena.getName());
                }
                
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to regenerate arena: " + arena.getName());
                e.printStackTrace();
            }
        });
    }
}