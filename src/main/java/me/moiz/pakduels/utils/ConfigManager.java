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
        loadScoreboardConfig();
    }
    
    // Scoreboard configuration
    private YamlConfiguration scoreboardConfig;
    
    private void loadScoreboardConfig() {
        File scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    public List<String> getScoreboardLines() {
        return scoreboardConfig.getStringList("lines");
    }
    
    public String getScoreboardTitle() {
        return scoreboardConfig.getString("title", "&6&lPakDuels");
    }
    
    public int getRoundDelay() {
        return config.getInt("duel.round-delay", 2);
    }
    
    public boolean hasSpawnSet() {
        return config.contains("spawn.world");
    }
    
    public void setSpawn(String world, double x, double y, double z, float yaw, float pitch) {
        config.set("spawn.world", world);
        config.set("spawn.x", x);
        config.set("spawn.y", y);
        config.set("spawn.z", z);
        config.set("spawn.yaw", yaw);
        config.set("spawn.pitch", pitch);
        plugin.saveConfig();
    }
}