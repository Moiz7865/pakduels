package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.SerializationUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitManager {
    private final PakDuelsPlugin plugin;
    private final Map<String, Kit> kits;
    private final File kitsFolder;
    
    public KitManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.kits = new ConcurrentHashMap<>();
        this.kitsFolder = new File(plugin.getDataFolder(), "kits");
        
        // Create kits folder if it doesn't exist
        if (!kitsFolder.exists()) {
            kitsFolder.mkdirs();
        }
    }
    
    public void loadKits() {
        kits.clear();
        
        File[] kitFiles = kitsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (kitFiles == null) {
            plugin.getLogger().info("No kit files found.");
            return;
        }
        
        for (File kitFile : kitFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(kitFile);
                String kitName = kitFile.getName().replace(".yml", "");
                
                List<String> contentsData = config.getStringList("contents");
                List<String> armorData = config.getStringList("armor");
                String offHandData = config.getString("offhand", "");
                
                Kit kit = new Kit(
                    kitName,
                    SerializationUtils.deserializeItemStackArray(contentsData),
                    SerializationUtils.deserializeItemStackArray(armorData),
                    SerializationUtils.deserializeItemStack(offHandData)
                );
                
                // Load rules
                if (config.contains("rules")) {
                    for (String key : config.getConfigurationSection("rules").getKeys(false)) {
                        kit.setRule(key, config.getBoolean("rules." + key));
                    }
                }
                
                kits.put(kitName.toLowerCase(), kit);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit: " + kitFile.getName());
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits.");
    }
    
    public void saveKit(Kit kit) {
        try {
            File kitFile = new File(kitsFolder, kit.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            
            config.set("contents", SerializationUtils.serializeItemStackArray(kit.getContents()));
            config.set("armor", SerializationUtils.serializeItemStackArray(kit.getArmorContents()));
            config.set("offhand", SerializationUtils.serializeItemStack(kit.getOffHand()));
            
            // Save rules
            for (Map.Entry<String, Boolean> entry : kit.getRules().entrySet()) {
                config.set("rules." + entry.getKey(), entry.getValue());
            }
            
            config.save(kitFile);
            plugin.getLogger().info("Saved kit: " + kit.getName());
            
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save kit: " + kit.getName());
            e.printStackTrace();
        }
    }
    
    public void saveKits() {
        for (Kit kit : kits.values()) {
            saveKit(kit);
        }
    }
    
    public void addKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
        saveKit(kit);
    }
    
    public void removeKit(String name) {
        kits.remove(name.toLowerCase());
        File kitFile = new File(kitsFolder, name + ".yml");
        if (kitFile.exists()) {
            kitFile.delete();
        }
    }
    
    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }
    
    public Collection<Kit> getAllKits() {
        return new ArrayList<>(kits.values());
    }
    
    public Set<String> getKitNames() {
        return kits.keySet();
    }
    
    public boolean hasKit(String name) {
        return kits.containsKey(name.toLowerCase());
    }
    
    public int getKitCount() {
        return kits.size();
    }
}