package com.turboauth.config;

import com.turboauth.TurboAuth;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigManager {

    private final TurboAuth plugin;
    private FileConfiguration config;
    private File configFile;

    // Config keys
    public static final String AUTH_SPAWN = "auth-spawn.world";
    public static final String AUTH_SPAWN_X = "auth-spawn.x";
    public static final String AUTH_SPAWN_Y = "auth-spawn.y";
    public static final String AUTH_SPAWN_Z = "auth-spawn.z";
    public static final String AUTH_SPAWN_YAW = "auth-spawn.yaw";
    public static final String AUTH_SPAWN_PITCH = "auth-spawn.pitch";

    public static final String FALLBACK_SPAWN = "fallback-spawn.world";
    public static final String FALLBACK_SPAWN_X = "fallback-spawn.x";
    public static final String FALLBACK_SPAWN_Y = "fallback-spawn.y";
    public static final String FALLBACK_SPAWN_Z = "fallback-spawn.z";
    public static final String FALLBACK_SPAWN_YAW = "fallback-spawn.yaw";
    public static final String FALLBACK_SPAWN_PITCH = "fallback-spawn.pitch";

    public ConfigManager(TurboAuth plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Set default values if not present
        setDefaults();
    }

    private void setDefaults() {
        config.addDefault("messages.join-not-registered", "&7&l⟶ &eWelcome to the server! &7Please &bregister &7with &e/register <password> <password>");
        config.addDefault("messages.join-registered", "&7&l⟶ &eWelcome back! &7Please &blogin &7with &e/login <password>");
        config.addDefault("messages.register-success", "&a&l✓ &7Successfully registered! &eWelcome to the server!");
        config.addDefault("messages.login-success", "&a&l✓ &7Successfully logged in! &eEnjoy your stay!");
        config.addDefault("messages.register-fail-already", "&c&l✗ &7You are already registered!");
        config.addDefault("messages.register-fail-mismatch", "&c&l✗ &7Passwords do not match!");
        config.addDefault("messages.login-fail-wrong", "&c&l✗ &7Wrong password! &7(&e{attempts}&7/&e{max}&7)");
        config.addDefault("messages.login-fail-not-registered", "&c&l✗ &7You are not registered! &7Use &e/register <password> <password>");
        config.addDefault("messages.kick-brute-force", "&c&l⚡ Protected by TurboAuth\n&7Reason: &eToo many failed login attempts");
        config.addDefault("messages.reminder", "&7&l⟶ &ePlease authenticate with &e/login <password>");

        config.addDefault("settings.max-login-attempts", 5);
        config.addDefault("settings.attempt-cooldown", 10);
        config.addDefault("settings.reminder-interval", 30);
        config.addDefault("settings.auto-login", false);
        config.addDefault("settings.enable-darkness", true);
        config.addDefault("settings.enable-freeze", true);
        config.addDefault("settings.ip-bans", false);
        config.addDefault("settings.ip-ban-duration", 3600L);

        // Animations
        config.addDefault("animations.join.title", "&e&lWelcome");
        config.addDefault("animations.join.subtitle", "&7Please authenticate");
        config.addDefault("animations.join.fade-in", 10);
        config.addDefault("animations.join.stay", 60);
        config.addDefault("animations.join.fade-out", 10);
        config.addDefault("animations.success.title", "&a&lSuccess");
        config.addDefault("animations.success.subtitle", "&7Authenticated!");
        config.addDefault("animations.success.fade-in", 5);
        config.addDefault("animations.success.stay", 40);
        config.addDefault("animations.success.fade-out", 5);

        // Sounds
        config.addDefault("sounds.join", "BLOCK_ANVIL_LAND");
        config.addDefault("sounds.register-success", "ENTITY_PLAYER_LEVELUP");
        config.addDefault("sounds.login-success", "ENTITY_EXPERIENCE_ORB_PICKUP");
        config.addDefault("sounds.login-fail", "ENTITY_VILLAGER_NO");
        config.addDefault("sounds.enabled", true);

        // Colors
        config.addDefault("colors.primary", "&e");
        config.addDefault("colors.secondary", "&7");
        config.addDefault("colors.success", "&a");
        config.addDefault("colors.error", "&c");
        config.addDefault("colors.accent", "&b");

        config.options().copyDefaults(true);
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save config: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Location getAuthSpawn() {
        String worldName = config.getString(AUTH_SPAWN);
        if (worldName == null) {
            return null;
        }

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(
            world,
            config.getDouble(AUTH_SPAWN_X),
            config.getDouble(AUTH_SPAWN_Y),
            config.getDouble(AUTH_SPAWN_Z),
            (float) config.getDouble(AUTH_SPAWN_YAW),
            (float) config.getDouble(AUTH_SPAWN_PITCH)
        );
    }

    public void setAuthSpawn(Location location) {
        config.set(AUTH_SPAWN, location.getWorld().getName());
        config.set(AUTH_SPAWN_X, location.getX());
        config.set(AUTH_SPAWN_Y, location.getY());
        config.set(AUTH_SPAWN_Z, location.getZ());
        config.set(AUTH_SPAWN_YAW, location.getYaw());
        config.set(AUTH_SPAWN_PITCH, location.getPitch());
        saveConfig();
    }

    public Location getFallbackSpawn() {
        String worldName = config.getString(FALLBACK_SPAWN);
        if (worldName == null) {
            return null;
        }

        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(
            world,
            config.getDouble(FALLBACK_SPAWN_X),
            config.getDouble(FALLBACK_SPAWN_Y),
            config.getDouble(FALLBACK_SPAWN_Z),
            (float) config.getDouble(FALLBACK_SPAWN_YAW),
            (float) config.getDouble(FALLBACK_SPAWN_PITCH)
        );
    }

    public void setFallbackSpawn(Location location) {
        config.set(FALLBACK_SPAWN, location.getWorld().getName());
        config.set(FALLBACK_SPAWN_X, location.getX());
        config.set(FALLBACK_SPAWN_Y, location.getY());
        config.set(FALLBACK_SPAWN_Z, location.getZ());
        config.set(FALLBACK_SPAWN_YAW, location.getYaw());
        config.set(FALLBACK_SPAWN_PITCH, location.getPitch());
        saveConfig();
    }

    public String getMessage(String path) {
        return config.getString(path);
    }

    public List<String> getMessages(String path) {
        return config.getStringList(path);
    }

    public int getMaxLoginAttempts() {
        return config.getInt("settings.max-login-attempts");
    }

    public int getAttemptCooldown() {
        return config.getInt("settings.attempt-cooldown");
    }

    public int getReminderInterval() {
        return config.getInt("settings.reminder-interval");
    }

    public boolean isAutoLogin() {
        return config.getBoolean("settings.auto-login");
    }

    public boolean isDarknessEnabled() {
        return config.getBoolean("settings.enable-darkness");
    }

    public boolean isFreezeEnabled() {
        return config.getBoolean("settings.enable-freeze");
    }

    public boolean isIpBans() {
        return config.getBoolean("settings.ip-bans");
    }

    public long getIpBanDuration() {
        return config.getLong("settings.ip-ban-duration");
    }
}
