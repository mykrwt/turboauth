package com.turboauth.auth;

import com.turboauth.TurboAuth;
import com.turboauth.config.ConfigManager;
import com.turboauth.storage.StorageManager;
import com.turboauth.utils.AnimationUtils;
import com.turboauth.utils.MessageUtils;
import com.turboauth.utils.PermissionUtils;
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

    public AuthManager(TurboAuth plugin, ConfigManager configManager, StorageManager storageManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.storageManager = storageManager;

        this.messageUtils = new MessageUtils(configManager);
        this.animationUtils = new AnimationUtils(plugin, configManager, messageUtils);

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

        storageManager.createPlayerData(player, password);

        messageUtils.sendMessage(player, configManager.getMessage("messages.register-success"));

        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.register-success"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }

        animationUtils.showSuccessAnimation(player);

        if (configManager.isAutoLogin()) {
            loginPlayer(player, password);
        } else {
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

        if (isOnCooldown(player)) {
            int remainingSeconds = getRemainingCooldown(player);
            messageUtils.sendMessage(player, "&c&l✗ &7Please wait &e" + remainingSeconds + " &7seconds before trying again!");
            return false;
        }

        StorageManager.PlayerData playerData = storageManager.getPlayerData(player.getUniqueId());
        if (playerData == null || !playerData.getPassword().equals(password)) {
            handleFailedLogin(player);
            return false;
        }

        handleSuccessfulLogin(player);
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

        String message = configManager.getMessage("messages.login-fail-wrong")
            .replace("{attempts}", String.valueOf(attempts))
            .replace("{max}", String.valueOf(maxAttempts));

        messageUtils.sendMessage(player, message);

        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.login-fail"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }
    }

    private void handleSuccessfulLogin(Player player) {
        UUID uuid = player.getUniqueId();

        failedAttempts.remove(uuid);
        lastAttempt.remove(uuid);

        stopReminderTask(uuid);

        storageManager.updatePlayerLogin(uuid, player);

        messageUtils.sendMessage(player, configManager.getMessage("messages.login-success"));

        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.login-success"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }

        animationUtils.showSuccessAnimation(player);
        restorePlayer(player);
    }

    private void handleBruteForce(Player player) {
        UUID uuid = player.getUniqueId();

        if (configManager.isIpBans() && player.getAddress() != null) {
            String playerIP = player.getAddress().getHostString();
            plugin.getLogger().warning("Brute force detected from IP: " + playerIP);
        }

        player.kickPlayer(messageUtils.colorize(configManager.getMessage("messages.kick-brute-force")));

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
        UUID uuid = player.getUniqueId();
        org.bukkit.Location savedLocation = storageManager.getSavedLocation(uuid);

        if (savedLocation != null && savedLocation.getWorld() != null) {
            player.teleport(savedLocation);
        } else {
            org.bukkit.Location fallback = configManager.getFallbackSpawn();
            if (fallback != null) {
                player.teleport(fallback);
            }
        }

        removePlayerRestrictions(player);
        storageManager.removeSavedLocation(uuid);
    }

    public void startReminderTask(Player player) {
        UUID uuid = player.getUniqueId();
        stopReminderTask(uuid);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    reminderTasks.remove(uuid);
                    return;
                }

                if (isAuthenticated(uuid)) {
                    this.cancel();
                    reminderTasks.remove(uuid);
                    return;
                }

                messageUtils.sendMessage(player, configManager.getMessage("messages.reminder"));
            }
        }.runTaskTimer(plugin, configManager.getReminderInterval() * 20L, configManager.getReminderInterval() * 20L);

        reminderTasks.put(uuid, task);
    }

    private boolean isAuthenticated(UUID uuid) {
        return !storageManager.getSavedLocationMap().containsKey(uuid);
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
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setContents(player.getInventory().getContents());

        org.bukkit.Location authSpawn = configManager.getAuthSpawn();
        if (authSpawn != null) {
            player.teleport(authSpawn);
        }

        if (configManager.isFreezeEnabled()) {
            player.setInvulnerable(true);
            player.setCollidable(false);
            player.setCanPickupItems(false);
            player.setCustomNameVisible(false);
            player.setCustomName("");
        }

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
        player.removePotionEffect(PotionEffectType.DARKNESS);

        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setCanPickupItems(true);
    }

    public void savePlayerLocationAndRestrict(Player player) {
        storageManager.savePlayerLocation(player);
        applyPlayerRestrictions(player);
    }
}
