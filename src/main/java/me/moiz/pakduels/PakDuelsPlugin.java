package me.moiz.pakduels;

import me.moiz.pakduels.commands.DuelCommand;
import me.moiz.pakduels.commands.PakMCCommand;
import me.moiz.pakduels.listeners.DuelListener;
import me.moiz.pakduels.listeners.GuiListener;
import me.moiz.pakduels.managers.*;
import me.moiz.pakduels.utils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PakDuelsPlugin extends JavaPlugin {
    
    private static PakDuelsPlugin instance;
    
    // Managers
    private ConfigManager configManager;
    private KitManager kitManager;
    private ArenaManager arenaManager;
    private DuelManager duelManager;
    private ScoreboardManager scoreboardManager;
    private GuiManager guiManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.kitManager = new KitManager(this);
        this.arenaManager = new ArenaManager(this);
        this.duelManager = new DuelManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.guiManager = new GuiManager(this);
        
        // Load data
        configManager.loadConfigs();
        kitManager.loadKits();
        arenaManager.loadArenas();
        
        // Register commands
        getCommand("duel").setExecutor(new DuelCommand(this));
        getCommand("pakmc").setExecutor(new PakMCCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        
        getLogger().info("PakDuels has been enabled!");
    }
    
    @Override
    public void onDisable() {
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
        if (scoreboardManager != null) {
            scoreboardManager.cleanup();
        }
        
        getLogger().info("PakDuels has been disabled!");
    }
    
    // Getters
    public static PakDuelsPlugin getInstance() {
        return instance;
    }
    
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
}