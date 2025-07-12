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
import java.io.IOException;

public class ArenaCloneManager {
    private final PakDuelsPlugin plugin;
    private final File schematicsFolder;
    
    public ArenaCloneManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
    }
    
    public void saveArenaSchematic(Arena arena) {
        if (!arena.isComplete()) {
            plugin.getLogger().warning("Cannot save schematic for incomplete arena: " + arena.getName());
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(arena.getPosition1().getWorld());
                
                // Create region from arena bounds
                BlockVector3 min = BlockVector3.at(
                    Math.min(arena.getPosition1().getBlockX(), arena.getPosition2().getBlockX()),
                    Math.min(arena.getPosition1().getBlockY(), arena.getPosition2().getBlockY()),
                    Math.min(arena.getPosition1().getBlockZ(), arena.getPosition2().getBlockZ())
                );
                
                BlockVector3 max = BlockVector3.at(
                    Math.max(arena.getPosition1().getBlockX(), arena.getPosition2().getBlockX()),
                    Math.max(arena.getPosition1().getBlockY(), arena.getPosition2().getBlockY()),
                    Math.max(arena.getPosition1().getBlockZ(), arena.getPosition2().getBlockZ())
                );
                
                CuboidRegion region = new CuboidRegion(world, min, max);
                
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                    Clipboard clipboard = editSession.lazyCopy(region);
                    
                    File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                    ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                    
                    if (format != null) {
                        try (ClipboardWriter writer = format.getWriter(new FileOutputStream(schematicFile))) {
                            writer.write(clipboard);
                            plugin.getLogger().info("Saved schematic for arena: " + arena.getName());
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save schematic for arena: " + arena.getName());
                e.printStackTrace();
            }
        });
    }
    
    public void cloneArenaSchematic(Arena originalArena, Arena clonedArena, Player player) {
        File schematicFile = new File(schematicsFolder, originalArena.getName() + ".schem");
        
        if (!schematicFile.exists()) {
            // Create schematic first
            saveArenaSchematic(originalArena);
            
            // Wait a bit and try again
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                cloneArenaSchematic(originalArena, clonedArena, player);
            }, 40L); // 2 seconds delay
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(clonedArena.getCenter().getWorld());
                
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    MessageUtils.sendMessage(player, "&cFailed to load schematic format!");
                    return;
                }
                
                Clipboard clipboard;
                try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                    clipboard = reader.read();
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
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        MessageUtils.sendMessage(player, "&aArena schematic pasted successfully!");
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to clone arena schematic: " + e.getMessage());
                e.printStackTrace();
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    MessageUtils.sendMessage(player, "&cFailed to paste arena schematic!");
                });
            }
        });
    }
    
    public void regenerateArena(Arena arena) {
        File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
        
        if (!schematicFile.exists()) {
            plugin.getLogger().warning("No schematic found for arena: " + arena.getName());
            return;
        }
        
        if (arena.getCenter() == null) {
            plugin.getLogger().warning("No center set for arena: " + arena.getName());
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(arena.getCenter().getWorld());
                
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    plugin.getLogger().warning("Failed to load schematic format for arena: " + arena.getName());
                    return;
                }
                
                Clipboard clipboard;
                try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
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
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to regenerate arena: " + arena.getName());
                e.printStackTrace();
            }
        });
    }
}