package com.turboauth;

import com.turboauth.auth.AuthManager;
import com.turboauth.commands.TurboAuthCommand;
import com.turboauth.config.ConfigManager;
import com.turboauth.events.PlayerEvents;
import com.turboauth.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TurboAuth extends JavaPlugin {
    
    private static TurboAuth instance;
    private ConfigManager configManager;
    private StorageManager storageManager;
    private AuthManager authManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.configManager = new ConfigManager();
        this.storageManager = new StorageManager();
        this.authManager = new AuthManager();
        
        // Load configuration and data
        configManager.loadConfig();
        storageManager.loadData();
        
        // Register commands
        registerCommands();
        
        // Register events
        registerEvents();
        
        getLogger().info("TurboAuth v" + getDescription().getVersion() + " enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        // Save all data on shutdown
        if (storageManager != null) {
            storageManager.saveAllData();
        }
        
        getLogger().info("TurboAuth disabled.");
    }
    
    private void registerCommands() {
        TurboAuthCommand command = new TurboAuthCommand();
        getCommand("turboauth").setExecutor(command);
        getCommand("turboauth").setTabCompleter(command);
        getCommand("login").setExecutor(command);
        getCommand("register").setExecutor(command);
    }
    
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
    }
    
    public static TurboAuth getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public StorageManager getStorageManager() {
        return storageManager;
    }
    
    public AuthManager getAuthManager() {
        return authManager;
    }
}