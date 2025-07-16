package me.moiz.pakduels;

import me.moiz.pakduels.commands.DuelCommand;
import me.moiz.pakduels.commands.PakMCCommand;
import me.moiz.pakduels.listeners.DuelListener;
import me.moiz.pakduels.listeners.GuiListener;
import me.moiz.pakduels.managers.*;
import me.moiz.pakduels.utils.ConfigManager;
import me.moiz.pakduels.utils.HealthDisplayManager;
import me.moiz.pakduels.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class PakDuelsPlugin extends JavaPlugin {
    
    private ConfigManager configManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private ScoreboardManager scoreboardManager;
    private GuiManager guiManager;
    private HealthDisplayManager healthDisplayManager;
    private ArenaCloneManager arenaCloneManager;
    
    @Override
    public void onEnable() {
        getLogger().info("PakDuels is starting up...");
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        
        // Initialize MessageUtils with plugin instance
        MessageUtils.setPlugin(this);
        
        this.kitManager = new KitManager(this);
        this.arenaManager = new ArenaManager(this);
        this.duelManager = new DuelManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.guiManager = new GuiManager(this);
        this.healthDisplayManager = new HealthDisplayManager(this);
        this.arenaCloneManager = new ArenaCloneManager(this);
        
        // Load configurations
        configManager.loadConfigs();
        
        // Load data
        kitManager.loadKits();
        arenaManager.loadArenas();
        
        // Register commands
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("pakmc").setExecutor(new PakMCCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        
        getLogger().info("PakDuels has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("PakDuels is shutting down...");
        
        // Save data
        if (kitManager != null) {
            kitManager.saveKits();
        }
        if (arenaManager != null) {
            arenaManager.saveArenas();
        }
        if (duelManager != null) {
            duelManager.endAllDuels();
        }
        if (guiManager != null) {
            guiManager.cleanup();
        }
        if (healthDisplayManager != null) {
            healthDisplayManager.cleanup();
        }
        
        getLogger().info("PakDuels has been disabled successfully!");
    }
    
    // Getters for managers
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public ArenaManager getArenaManager() {
        return arenaManager;
    }
    
    public DuelManager getDuelManager() {
        return duelManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    public HealthDisplayManager getHealthDisplayManager() {
        return healthDisplayManager;
    }
    
    public ArenaCloneManager getArenaCloneManager() {
        return arenaCloneManager;
    }
}