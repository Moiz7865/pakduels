package me.moiz.pakduels.utils;

import me.moiz.pakduels.PakDuelsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final PakDuelsPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public int getInventoryCountdownTime() {
        return config.getInt("duel.inventory-countdown-time", 10);
    }
    
    public boolean isHealthIndicatorsEnabled() {
        return config.getBoolean("duel.health-indicators", true);
    }
    
    public String getSpawnWorldName() {
        return config.getString("spawn.world", "world");
    }
    
    public double getSpawnX() {
        return config.getDouble("spawn.x", 0.0);
    }
    
    public double getSpawnY() {
        return config.getDouble("spawn.y", 64.0);
    }
    
    public double getSpawnZ() {
        return config.getDouble("spawn.z", 0.0);
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}