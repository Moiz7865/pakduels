package me.moiz.pakduels.managers;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final PakDuelsPlugin plugin;
    private final Map<String, Arena> arenas;
    private final File arenasFile;
    private final File schematicsFolder;
    private YamlConfiguration arenasConfig;
    
    public ArenaManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new ConcurrentHashMap<>();
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        this.schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        
        // Create files and folders if they don't exist
        if (!arenasFile.exists()) {
            try {
                arenasFile.getParentFile().mkdirs();
                arenasFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create arenas.yml file!");
                e.printStackTrace();
            }
        }
        
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
        }
        
        this.arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
    }
    
    public void loadArenas() {
        arenas.clear();
        
        if (!arenasConfig.contains("arenas")) {
            plugin.getLogger().info("No arenas found in configuration.");
            return;
        }
        
        ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
        if (arenasSection == null) return;
        
        for (String arenaName : arenasSection.getKeys(false)) {
            try {
                ConfigurationSection arenaSection = arenasSection.getConfigurationSection(arenaName);
                if (arenaSection == null) continue;
                
                Arena arena = new Arena(arenaName);
                
                // Load positions
                if (arenaSection.contains("position1")) {
                    arena.setPosition1(SerializationUtils.deserializeLocation(arenaSection.getString("position1")));
                }
                if (arenaSection.contains("position2")) {
                    arena.setPosition2(SerializationUtils.deserializeLocation(arenaSection.getString("position2")));
                }
                if (arenaSection.contains("spawn1")) {
                    arena.setSpawnPoint1(SerializationUtils.deserializeLocation(arenaSection.getString("spawn1")));
                }
                if (arenaSection.contains("spawn2")) {
                    arena.setSpawnPoint2(SerializationUtils.deserializeLocation(arenaSection.getString("spawn2")));
                }
                
                // Load settings
                arena.setRegenerationEnabled(arenaSection.getBoolean("regeneration", true));
                arena.setAllowedKits(arenaSection.getStringList("allowed-kits"));
                
                arenas.put(arenaName.toLowerCase(), arena);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load arena: " + arenaName);
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }
    
    public void saveArenas() {
        arenasConfig.set("arenas", null); // Clear existing arenas
        
        for (Arena arena : arenas.values()) {
            try {
                String path = "arenas." + arena.getName();
                
                if (arena.getPosition1() != null) {
                    arenasConfig.set(path + ".position1", SerializationUtils.serializeLocation(arena.getPosition1()));
                }
                if (arena.getPosition2() != null) {
                    arenasConfig.set(path + ".position2", SerializationUtils.serializeLocation(arena.getPosition2()));
                }
                if (arena.getSpawnPoint1() != null) {
                    arenasConfig.set(path + ".spawn1", SerializationUtils.serializeLocation(arena.getSpawnPoint1()));
                }
                if (arena.getSpawnPoint2() != null) {
                    arenasConfig.set(path + ".spawn2", SerializationUtils.serializeLocation(arena.getSpawnPoint2()));
                }
                
                arenasConfig.set(path + ".regeneration", arena.isRegenerationEnabled());
                arenasConfig.set(path + ".allowed-kits", arena.getAllowedKits());
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save arena: " + arena.getName());
                e.printStackTrace();
            }
        }
        
        try {
            arenasConfig.save(arenasFile);
            plugin.getLogger().info("Saved " + arenas.size() + " arenas.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arenas.yml file!");
            e.printStackTrace();
        }
    }
    
    public void addArena(Arena arena) {
        arenas.put(arena.getName().toLowerCase(), arena);
        saveArenas();
    }
    
    public void removeArena(String name) {
        arenas.remove(name.toLowerCase());
        saveArenas();
    }
    
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }
    
    public Collection<Arena> getAllArenas() {
        return new ArrayList<>(arenas.values());
    }
    
    public Set<String> getArenaNames() {
        return arenas.keySet();
    }
    
    public boolean hasArena(String name) {
        return arenas.containsKey(name.toLowerCase());
    }
    
    public Arena getAvailableArena() {
        return arenas.values().stream()
                .filter(arena -> !arena.isInUse() && arena.isComplete())
                .findFirst()
                .orElse(null);
    }
    
    public void saveSchematic(Arena arena) {
        if (!arena.isComplete() || !arena.isRegenerationEnabled()) {
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Location pos1 = arena.getPosition1();
                Location pos2 = arena.getPosition2();
                
                BlockVector3 min = BlockVector3.at(
                        Math.min(pos1.getBlockX(), pos2.getBlockX()),
                        Math.min(pos1.getBlockY(), pos2.getBlockY()),
                        Math.min(pos1.getBlockZ(), pos2.getBlockZ())
                );
                
                BlockVector3 max = BlockVector3.at(
                        Math.max(pos1.getBlockX(), pos2.getBlockX()),
                        Math.max(pos1.getBlockY(), pos2.getBlockY()),
                        Math.max(pos1.getBlockZ(), pos2.getBlockZ())
                );
                
                CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(pos1.getWorld()), min, max);
                
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pos1.getWorld()))) {
                    Clipboard clipboard = Clipboard.create(region);
                    ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
                    Operations.complete(copy);
                    
                    File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                    try (FileOutputStream fos = new FileOutputStream(schematicFile);
                         ClipboardWriter writer = ClipboardFormats.findByFile(schematicFile).getWriter(fos)) {
                        writer.write(clipboard);
                    }
                    
                    plugin.getLogger().info("Saved schematic for arena: " + arena.getName());
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save schematic for arena: " + arena.getName());
                e.printStackTrace();
            }
        });
    }
    
    public void pasteSchematic(Arena arena) {
        if (!arena.isComplete() || !arena.isRegenerationEnabled()) {
            return;
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                File schematicFile = new File(schematicsFolder, arena.getName() + ".schem");
                if (!schematicFile.exists()) {
                    plugin.getLogger().warning("Schematic file not found for arena: " + arena.getName());
                    return;
                }
                
                Location pos1 = arena.getPosition1();
                
                try (FileInputStream fis = new FileInputStream(schematicFile);
                     ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(fis)) {
                    
                    Clipboard clipboard = reader.read();
                    
                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(pos1.getWorld()))) {
                        Operations.complete(new ClipboardHolder(clipboard).createPaste(editSession)
                                .to(BlockVector3.at(pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ()))
                                .ignoreAirBlocks(false)
                                .build());
                    }
                    
                    plugin.getLogger().info("Pasted schematic for arena: " + arena.getName());
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to paste schematic for arena: " + arena.getName());
                e.printStackTrace();
            }
        });
    }
    
    public int getArenaCount() {
        return arenas.size();
    }
}