package com.turboauth;

import com.turboauth.auth.AuthManager;
import com.turboauth.commands.TurboAuthCommand;
import com.turboauth.config.ConfigManager;
import com.turboauth.events.PlayerEvents;
import com.turboauth.storage.StorageManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TurboAuth extends JavaPlugin {

    private ConfigManager configManager;
    private StorageManager storageManager;
    private AuthManager authManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.configManager.initConfig();

        this.storageManager = new StorageManager(this);
        this.storageManager.initStorage();
        this.storageManager.loadData();

        this.authManager = new AuthManager(this, configManager, storageManager);

        registerCommands();
        registerEvents();

        getLogger().info("TurboAuth v" + getDescription().getVersion() + " enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (authManager != null) {
            authManager.stopAllTasks();
        }

        if (storageManager != null) {
            storageManager.saveAllData();
        }

        getLogger().info("TurboAuth disabled.");
    }

    private void registerCommands() {
        TurboAuthCommand command = new TurboAuthCommand(this, authManager, configManager, storageManager);

        if (getCommand("turboauth") != null) {
            getCommand("turboauth").setExecutor(command);
            getCommand("turboauth").setTabCompleter(command);
        }

        if (getCommand("login") != null) {
            getCommand("login").setExecutor(command);
        }

        if (getCommand("register") != null) {
            getCommand("register").setExecutor(command);
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(this, authManager, configManager, storageManager), this);
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
