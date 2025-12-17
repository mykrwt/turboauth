package com.turboauth.storage;

import com.turboauth.TurboAuth;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StorageManager {
    
    private final TurboAuth plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> playerDataMap;
    private final Map<UUID, Location> savedLocations;
    
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }
    
    public Map<UUID, Location> getSavedLocationMap() {
        return savedLocations;
    }
    
    public StorageManager() {
        this.plugin = TurboAuth.getInstance();
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        this.playerDataMap = new HashMap<>();
        this.savedLocations = new HashMap<>();
        
        // Ensure data folder exists
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }
    
    public void loadData() {
        // Load player data files
        File[] playerFiles = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (playerFiles != null) {
            for (File file : playerFiles) {
                String uuidStr = file.getName().replace(".yml", "");
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    PlayerData data = loadPlayerData(uuid);
                    if (data != null) {
                        playerDataMap.put(uuid, data);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in filename: " + uuidStr);
                }
            }
        }
        
        // Load saved locations
        loadSavedLocations();
        
        plugin.getLogger().info("Loaded data for " + playerDataMap.size() + " players");
    }
    
    private PlayerData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) {
            return null;
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            PlayerData data = new PlayerData();
            data.setUuid(uuid);
            data.setUsername(config.getString("username"));
            data.setPassword(config.getString("password"));
            data.setRegistrationDate(config.getString("registration-date"));
            data.setLastLoginDate(config.getString("last-login-date"));
            data.setLastKnownIP(config.getString("last-ip"));
            return data;
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading player data for " + uuid + ": " + e.getMessage());
            return null;
        }
    }
    
    private void loadSavedLocations() {
        File locationsFile = new File(plugin.getDataFolder(), "saved-locations.yml");
        if (locationsFile.exists()) {
            try {
                FileConfiguration config = YamlConfiguration.loadConfiguration(locationsFile);
                for (String uuidStr : config.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        String worldName = config.getString(uuidStr + ".world");
                        if (worldName != null) {
                            Location location = new Location(
                                plugin.getServer().getWorld(worldName),
                                config.getDouble(uuidStr + ".x"),
                                config.getDouble(uuidStr + ".y"),
                                config.getDouble(uuidStr + ".z"),
                                (float) config.getDouble(uuidStr + ".yaw"),
                                (float) config.getDouble(uuidStr + ".pitch")
                            );
                            savedLocations.put(uuid, location);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error loading saved location for UUID: " + uuidStr);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading saved locations: " + e.getMessage());
            }
        }
    }
    
    public void savePlayerData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;
        
        File file = new File(dataFolder, uuid + ".yml");
        
        try {
            FileConfiguration config = new YamlConfiguration();
            config.set("username", data.getUsername());
            config.set("password", data.getPassword());
            config.set("registration-date", data.getRegistrationDate());
            config.set("last-login-date", data.getLastLoginDate());
            config.set("last-ip", data.getLastKnownIP());
            
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving player data for " + uuid + ": " + e.getMessage());
        }
    }
    
    public void saveAllData() {
        for (UUID uuid : playerDataMap.keySet()) {
            savePlayerData(uuid);
        }
        saveSavedLocations();
    }
    
    private void saveSavedLocations() {
        File locationsFile = new File(plugin.getDataFolder(), "saved-locations.yml");
        
        try {
            FileConfiguration config = new YamlConfiguration();
            for (Map.Entry<UUID, Location> entry : savedLocations.entrySet()) {
                Location location = entry.getValue();
                String uuidStr = entry.getKey().toString();
                config.set(uuidStr + ".world", location.getWorld().getName());
                config.set(uuidStr + ".x", location.getX());
                config.set(uuidStr + ".y", location.getY());
                config.set(uuidStr + ".z", location.getZ());
                config.set(uuidStr + ".yaw", location.getYaw());
                config.set(uuidStr + ".pitch", location.getPitch());
            }
            config.save(locationsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving saved locations: " + e.getMessage());
        }
    }
    
    public void savePlayerLocation(Player player) {
        savedLocations.put(player.getUniqueId(), player.getLocation());
        saveSavedLocations();
    }
    
    public void removeSavedLocation(UUID uuid) {
        savedLocations.remove(uuid);
        saveSavedLocations();
    }
    
    public Location getSavedLocation(UUID uuid) {
        return savedLocations.get(uuid);
    }
    
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }
    
    public PlayerData getPlayerData(String username) {
        return playerDataMap.values().stream()
            .filter(data -> data.getUsername().equalsIgnoreCase(username))
            .findFirst()
            .orElse(null);
    }
    
    public void createPlayerData(Player player, String password) {
        PlayerData data = new PlayerData();
        data.setUuid(player.getUniqueId());
        data.setUsername(player.getName());
        data.setPassword(password); // Plain text as required
        data.setRegistrationDate(getCurrentDateTime());
        data.setLastLoginDate(getCurrentDateTime());
        data.setLastKnownIP(player.getAddress().getHostString());
        
        playerDataMap.put(player.getUniqueId(), data);
        savePlayerData(player.getUniqueId());
    }
    
    public void updatePlayerLogin(UUID uuid, Player player) {
        PlayerData data = playerDataMap.get(uuid);
        if (data != null) {
            data.setLastLoginDate(getCurrentDateTime());
            data.setLastKnownIP(player.getAddress().getHostString());
            savePlayerData(uuid);
        }
    }
    
    public boolean playerExists(UUID uuid) {
        return playerDataMap.containsKey(uuid);
    }
    
    public boolean playerExists(String username) {
        return playerDataMap.values().stream()
            .anyMatch(data -> data.getUsername().equalsIgnoreCase(username));
    }
    
    public int playerDataSize() {
        return playerDataMap.size();
    }
    
    private String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public static class PlayerData {
        private UUID uuid;
        private String username;
        private String password; // Plain text as required
        private String registrationDate;
        private String lastLoginDate;
        private String lastKnownIP;
        
        // Getters and setters
        public UUID getUuid() { return uuid; }
        public void setUuid(UUID uuid) { this.uuid = uuid; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRegistrationDate() { return registrationDate; }
        public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }
        
        public String getLastLoginDate() { return lastLoginDate; }
        public void setLastLoginDate(String lastLoginDate) { this.lastLoginDate = lastLoginDate; }
        
        public String getLastKnownIP() { return lastKnownIP; }
        public void setLastKnownIP(String lastKnownIP) { this.lastKnownIP = lastKnownIP; }
    }
}