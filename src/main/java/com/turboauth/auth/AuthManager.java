package com.turboauth.auth;

import com.turboauth.TurboAuth;
import com.turboauth.config.ConfigManager;
import com.turboauth.storage.StorageManager;
import com.turboauth.utils.AnimationUtils;
import com.turboauth.utils.MessageUtils;
import com.turboauth.utils.PermissionUtils;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthManager {
    
    private final TurboAuth plugin;
    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final AnimationUtils animationUtils;
    private final MessageUtils messageUtils;
    
    private final Map<UUID, Integer> failedAttempts;
    private final Map<UUID, LocalDateTime> lastAttempt;
    private final Map<UUID, BukkitTask> reminderTasks;
    
    public AuthManager() {
        this.plugin = TurboAuth.getInstance();
        this.configManager = plugin.getConfigManager();
        this.storageManager = plugin.getStorageManager();
        this.animationUtils = new AnimationUtils();
        this.messageUtils = new MessageUtils();
        this.failedAttempts = new HashMap<>();
        this.lastAttempt = new HashMap<>();
        this.reminderTasks = new HashMap<>();
    }
    
    public boolean registerPlayer(Player player, String password, String confirmPassword) {
        if (!PermissionUtils.hasPermission(player, "turboauth.register")) {
            messageUtils.sendMessage(player, "&c&l✗ &7You don't have permission to register!");
            return false;
        }
        
        if (storageManager.playerExists(player.getUniqueId())) {
            messageUtils.sendMessage(player, configManager.getMessage("messages.register-fail-already"));
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            messageUtils.sendMessage(player, configManager.getMessage("messages.register-fail-mismatch"));
            return false;
        }
        
        if (password.length() < 3) {
            messageUtils.sendMessage(player, "&c&l✗ &7Password must be at least 3 characters long!");
            return false;
        }
        
        // Create player data
        storageManager.createPlayerData(player, password);
        
        // Send success message
        messageUtils.sendMessage(player, configManager.getMessage("messages.register-success"));
        
        // Play success sound
        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.register-success"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }
        
        // Show success animation
        animationUtils.showSuccessAnimation(player);
        
        // Auto-login if enabled
        if (configManager.isAutoLogin()) {
            loginPlayer(player, password);
        } else {
            // Start reminder task for login
            startReminderTask(player);
        }
        
        return true;
    }
    
    public boolean loginPlayer(Player player, String password) {
        if (!PermissionUtils.hasPermission(player, "turboauth.login")) {
            messageUtils.sendMessage(player, "&c&l✗ &7You don't have permission to login!");
            return false;
        }
        
        if (!storageManager.playerExists(player.getUniqueId())) {
            messageUtils.sendMessage(player, configManager.getMessage("messages.login-fail-not-registered"));
            return false;
        }
        
        // Check attempt cooldown
        if (isOnCooldown(player)) {
            int remainingSeconds = getRemainingCooldown(player);
            messageUtils.sendMessage(player, "&c&l✗ &7Please wait &e" + remainingSeconds + " &7seconds before trying again!");
            return false;
        }
        
        // Check if password matches
        StorageManager.PlayerData playerData = storageManager.getPlayerData(player.getUniqueId());
        if (!playerData.getPassword().equals(password)) {
            handleFailedLogin(player);
            return false;
        }
        
        // Successful login
        handleSuccessfulLogin(player, playerData);
        return true;
    }
    
    private void handleFailedLogin(Player player) {
        UUID uuid = player.getUniqueId();
        failedAttempts.put(uuid, failedAttempts.getOrDefault(uuid, 0) + 1);
        lastAttempt.put(uuid, LocalDateTime.now());
        
        int attempts = failedAttempts.get(uuid);
        int maxAttempts = configManager.getMaxLoginAttempts();
        
        if (attempts >= maxAttempts) {
            handleBruteForce(player);
            return;
        }
        
        // Send failed login message
        String message = configManager.getMessage("messages.login-fail-wrong")
            .replace("{attempts}", String.valueOf(attempts))
            .replace("{max}", String.valueOf(maxAttempts));
        messageUtils.sendMessage(player, message);
        
        // Play failed sound
        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.login-fail"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }
    }
    
    private void handleSuccessfulLogin(Player player, StorageManager.PlayerData playerData) {
        UUID uuid = player.getUniqueId();
        
        // Clear failed attempts
        failedAttempts.remove(uuid);
        lastAttempt.remove(uuid);
        
        // Stop reminder task
        stopReminderTask(uuid);
        
        // Update player login data
        storageManager.updatePlayerLogin(uuid, player);
        
        // Send success message
        messageUtils.sendMessage(player, configManager.getMessage("messages.login-success"));
        
        // Play success sound
        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.login-success"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }
        
        // Show success animation
        animationUtils.showSuccessAnimation(player);
        
        // Remove restrictions and restore player
        restorePlayer(player);
    }
    
    private void handleBruteForce(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Optional IP ban
        if (configManager.isIpBans()) {
            String playerIP = player.getAddress().getHostString();
            long duration = configManager.getIpBanDuration();
            
            // Here you would implement IP ban logic
            // For now, we'll just log it
            plugin.getLogger().warning("Brute force detected from IP: " + playerIP);
        }
        
        // Kick player
        player.kickPlayer(messageUtils.colorize(configManager.getMessage("messages.kick-brute-force")));
        
        // Clear failed attempts
        failedAttempts.remove(uuid);
        lastAttempt.remove(uuid);
    }
    
    private boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        if (!lastAttempt.containsKey(uuid)) {
            return false;
        }
        
        LocalDateTime lastTime = lastAttempt.get(uuid);
        LocalDateTime now = LocalDateTime.now();
        long secondsBetween = java.time.Duration.between(lastTime, now).getSeconds();
        
        return secondsBetween < configManager.getAttemptCooldown();
    }
    
    private int getRemainingCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        LocalDateTime lastTime = lastAttempt.get(uuid);
        LocalDateTime now = LocalDateTime.now();
        long secondsBetween = java.time.Duration.between(lastTime, now).getSeconds();
        
        return (int) (configManager.getAttemptCooldown() - secondsBetween);
    }
    
    private void restorePlayer(Player player) {
        // Teleport to saved location or fallback
        UUID uuid = player.getUniqueId();
        org.bukkit.Location savedLocation = storageManager.getSavedLocation(uuid);
        
        if (savedLocation != null && savedLocation.getWorld() != null) {
            player.teleport(savedLocation);
            storageManager.removeSavedLocation(uuid);
        } else {
            // Use fallback spawn
            org.bukkit.Location fallback = configManager.getFallbackSpawn();
            if (fallback != null) {
                player.teleport(fallback);
            }
        }
        
        // Remove restrictions
        removePlayerRestrictions(player);
        
        // Save the location removal
        storageManager.removeSavedLocation(uuid);
    }
    
    public void startReminderTask(Player player) {
        UUID uuid = player.getUniqueId();
        stopReminderTask(uuid); // Stop existing task if any
        
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    reminderTasks.remove(uuid);
                    return;
                }
                
                // Check if player is still not authenticated
                if (!storageManager.playerExists(uuid)) {
                    this.cancel();
                    reminderTasks.remove(uuid);
                    return;
                }
                
                // Send reminder message
                messageUtils.sendMessage(player, configManager.getMessage("messages.reminder"));
            }
        }.runTaskTimer(plugin, configManager.getReminderInterval() * 20L, configManager.getReminderInterval() * 20L);
        
        reminderTasks.put(uuid, task);
    }
    
    public void stopReminderTask(UUID uuid) {
        BukkitTask task = reminderTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
    
    public void stopAllTasks() {
        for (BukkitTask task : reminderTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
        reminderTasks.clear();
    }
    
    public void applyPlayerRestrictions(Player player) {
        // Clear inventory and armor
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setContents(player.getInventory().getContents());
        
        // Teleport to auth spawn
        org.bukkit.Location authSpawn = configManager.getAuthSpawn();
        if (authSpawn != null) {
            player.teleport(authSpawn);
        }
        
        // Freeze player
        if (configManager.isFreezeEnabled()) {
            player.setInvulnerable(true);
            player.setCollidable(false);
            player.setCanPickupItems(false);
            player.setCustomNameVisible(false);
            player.setCustomName("");
        }
        
        // Apply darkness effect if enabled
        if (configManager.isDarknessEnabled()) {
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.DARKNESS, 
                Integer.MAX_VALUE, 
                1, 
                false, 
                false, 
                false
            ));
        }
    }
    
    private void removePlayerRestrictions(Player player) {
        // Remove darkness effect
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.DARKNESS);
        
        // Remove invulnerability
        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setCanPickupItems(true);
    }
    
    public void savePlayerLocationAndRestrict(Player player) {
        // Save original location
        storageManager.savePlayerLocation(player);
        
        // Apply restrictions
        applyPlayerRestrictions(player);
    }
}