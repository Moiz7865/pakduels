package me.moiz.pakduels.utils;

import me.moiz.pakduels.PakDuelsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final PakDuelsPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void loadConfigs() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        
        // Load configuration
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        // Set default values
        config.addDefault("settings.duel-request-timeout", 30);
        config.addDefault("settings.max-concurrent-duels", 10);
        config.addDefault("settings.allow-spectating", true);
        config.addDefault("settings.broadcast-results", true);
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
        
        plugin.getLogger().info("Configuration loaded successfully!");
    }
    
    public int getDuelRequestTimeout() {
        return config.getInt("settings.duel-request-timeout", 30);
    }
    
    public int getMaxConcurrentDuels() {
        return config.getInt("settings.max-concurrent-duels", 10);
    }
    
    public boolean isSpectatingAllowed() {
        return config.getBoolean("settings.allow-spectating", true);
    }
    
    public boolean isBroadcastResults() {
        return config.getBoolean("settings.broadcast-results", true);
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
}