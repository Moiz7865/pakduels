package me.moiz.pakduels.utils;

import me.moiz.pakduels.PakDuelsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigManager {
    private final PakDuelsPlugin plugin;
    private FileConfiguration config;
    private YamlConfiguration scoreboardConfig;
    private YamlConfiguration messagesConfig;
    
    public ConfigManager(PakDuelsPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadScoreboardConfig();
        loadMessagesConfig();
    }
    
    private void loadScoreboardConfig() {
        File scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
    
    private void loadMessagesConfig() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadScoreboardConfig();
        loadMessagesConfig();
    }
    
    // Duel settings
    public int getInventoryCountdownTime() {
        return config.getInt("duel.inventory-countdown-time", 10);
    }
    
    public int getRoundDelay() {
        return config.getInt("duel.round-delay", 2);
    }
    
    // Spawn settings
    public boolean hasSpawnSet() {
        return config.contains("spawn.world");
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
    
    public float getSpawnYaw() {
        return (float) config.getDouble("spawn.yaw", 0.0);
    }
    
    public float getSpawnPitch() {
        return (float) config.getDouble("spawn.pitch", 0.0);
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
    
    // Scoreboard configuration
    public List<String> getScoreboardLines() {
        return scoreboardConfig.getStringList("lines");
    }
    
    public String getScoreboardTitle() {
        return scoreboardConfig.getString("title", "&6&lPakDuels");
    }
    
    // Messages configuration
    public String getMessage(String key) {
        return messagesConfig.getString("messages." + key, "Message not found: " + key);
    }
    
    public boolean isMessageEnabled(String key) {
        return messagesConfig.getBoolean("messages." + key + ".enabled", true);
    }
    
    public String getMessageText(String key) {
        return messagesConfig.getString("messages." + key + ".text", "Message not found: " + key);
    }
}