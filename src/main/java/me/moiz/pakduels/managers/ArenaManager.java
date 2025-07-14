package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Arena;
import me.moiz.pakduels.utils.SerializationUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final PakDuelsPlugin plugin;
    private final Map<String, Arena> arenas;
    private final File arenasFolder;
    
    public ArenaManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.arenas = new ConcurrentHashMap<>();
        this.arenasFolder = new File(plugin.getDataFolder(), "arenas");
        
        // Create arenas folder if it doesn't exist
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }
    }
    
    public void loadArenas() {
        arenas.clear();
        
        File[] arenaFiles = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (arenaFiles == null) {
            plugin.getLogger().info("No arena files found.");
            return;
        }
        
        for (File arenaFile : arenaFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
                String arenaName = arenaFile.getName().replace(".yml", "");
                
                Location pos1 = SerializationUtils.deserializeLocation(config.getString("position1"));
                Location pos2 = SerializationUtils.deserializeLocation(config.getString("position2"));
                Location spawn1 = SerializationUtils.deserializeLocation(config.getString("spawn1"));
                Location spawn2 = SerializationUtils.deserializeLocation(config.getString("spawn2"));
                Location center = SerializationUtils.deserializeLocation(config.getString("center"));
                
                Arena arena = new Arena(arenaName, pos1, pos2, spawn1, spawn2);
                arena.setCenter(center);
                arena.setAllowedKits(config.getStringList("allowed-kits"));
                arena.setRegenerationEnabled(config.getBoolean("regeneration", false));
                
                arenas.put(arenaName.toLowerCase(), arena);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load arena: " + arenaFile.getName());
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas.");
    }
    
    public void saveArena(Arena arena) {
        try {
            File arenaFile = new File(arenasFolder, arena.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            
            config.set("position1", SerializationUtils.serializeLocation(arena.getPosition1()));
            config.set("position2", SerializationUtils.serializeLocation(arena.getPosition2()));
            config.set("spawn1", SerializationUtils.serializeLocation(arena.getSpawnPoint1()));
            config.set("spawn2", SerializationUtils.serializeLocation(arena.getSpawnPoint2()));
            config.set("center", SerializationUtils.serializeLocation(arena.getCenter()));
            config.set("allowed-kits", arena.getAllowedKits());
            config.set("regeneration", arena.isRegenerationEnabled());
            
            config.save(arenaFile);
            plugin.getLogger().info("Saved arena: " + arena.getName());
            
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arena: " + arena.getName());
            e.printStackTrace();
        }
    }
    
    public void saveArenas() {
        for (Arena arena : arenas.values()) {
            saveArena(arena);
        }
    }
    
    public void addArena(Arena arena) {
        arenas.put(arena.getName().toLowerCase(), arena);
        saveArena(arena);
    }
    
    public void removeArena(String name) {
        arenas.remove(name.toLowerCase());
        File arenaFile = new File(arenasFolder, name + ".yml");
        if (arenaFile.exists()) {
            arenaFile.delete();
        }
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
    
    public Arena getAvailableArena(String kitName) {
        return arenas.values().stream()
                .filter(arena -> !arena.isReserved())
                .filter(arena -> arena.isKitAllowed(kitName))
                .filter(Arena::isComplete)
                .findFirst()
                .orElse(null);
    }
    
    public int getArenaCount() {
        return arenas.size();
    }
}