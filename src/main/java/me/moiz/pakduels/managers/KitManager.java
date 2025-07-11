package me.moiz.pakduels.managers;

import me.moiz.pakduels.PakDuelsPlugin;
import me.moiz.pakduels.models.Kit;
import me.moiz.pakduels.utils.SerializationUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KitManager {
    private final PakDuelsPlugin plugin;
    private final Map<String, Kit> kits;
    private final File kitsFile;
    private YamlConfiguration kitsConfig;
    
    public KitManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.kits = new ConcurrentHashMap<>();
        this.kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        
        // Create file if it doesn't exist
        if (!kitsFile.exists()) {
            try {
                kitsFile.getParentFile().mkdirs();
                kitsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create kits.yml file!");
                e.printStackTrace();
            }
        }
        
        this.kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
    }
    
    public void loadKits() {
        kits.clear();
        
        if (!kitsConfig.contains("kits")) {
            plugin.getLogger().info("No kits found in configuration.");
            return;
        }
        
        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        if (kitsSection == null) return;
        
        for (String kitName : kitsSection.getKeys(false)) {
            try {
                ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitName);
                if (kitSection == null) continue;
                
                ItemStack[] contents = SerializationUtils.deserializeItemStackArray(
                        kitSection.getStringList("contents"));
                ItemStack[] armorContents = SerializationUtils.deserializeItemStackArray(
                        kitSection.getStringList("armor"));
                ItemStack offHand = SerializationUtils.deserializeItemStack(
                        kitSection.getString("offhand"));
                
                Kit kit = new Kit(kitName, contents, armorContents, offHand);
                kits.put(kitName.toLowerCase(), kit);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load kit: " + kitName);
                e.printStackTrace();
            }
        }
        
        plugin.getLogger().info("Loaded " + kits.size() + " kits.");
    }
    
    public void saveKits() {
        kitsConfig.set("kits", null); // Clear existing kits
        
        for (Kit kit : kits.values()) {
            try {
                String path = "kits." + kit.getName();
                
                kitsConfig.set(path + ".contents", 
                        SerializationUtils.serializeItemStackArray(kit.getContents()));
                kitsConfig.set(path + ".armor", 
                        SerializationUtils.serializeItemStackArray(kit.getArmorContents()));
                kitsConfig.set(path + ".offhand", 
                        SerializationUtils.serializeItemStack(kit.getOffHand()));
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to save kit: " + kit.getName());
                e.printStackTrace();
            }
        }
        
        try {
            kitsConfig.save(kitsFile);
            plugin.getLogger().info("Saved " + kits.size() + " kits.");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save kits.yml file!");
            e.printStackTrace();
        }
    }
    
    public void addKit(Kit kit) {
        kits.put(kit.getName().toLowerCase(), kit);
        saveKits();
    }
    
    public void removeKit(String name) {
        kits.remove(name.toLowerCase());
        saveKits();
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