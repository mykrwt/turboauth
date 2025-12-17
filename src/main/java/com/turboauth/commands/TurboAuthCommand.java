package com.turboauth.commands;

import com.turboauth.TurboAuth;
import com.turboauth.auth.AuthManager;
import com.turboauth.config.ConfigManager;
import com.turboauth.storage.StorageManager;
import com.turboauth.utils.MessageUtils;
import com.turboauth.utils.PermissionUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class TurboAuthCommand implements CommandExecutor, TabCompleter {
    
    private final TurboAuth plugin;
    private final AuthManager authManager;
    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final MessageUtils messageUtils;
    
    public TurboAuthCommand() {
        this.plugin = TurboAuth.getInstance();
        this.authManager = plugin.getAuthManager();
        this.configManager = plugin.getConfigManager();
        this.storageManager = plugin.getStorageManager();
        this.messageUtils = new MessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String commandName = cmd.getName().toLowerCase();
        
        if (commandName.equals("login")) {
            return handleLoginCommand(sender, args);
        } else if (commandName.equals("register")) {
            return handleRegisterCommand(sender, args);
        } else if (commandName.equals("turboauth")) {
            return handleTurboAuthCommand(sender, args);
        }
        
        return false;
    }
    
    private boolean handleLoginCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "&c&l✗ &7This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 1) {
            messageUtils.sendMessage(player, "&c&l✗ &7Usage: &e/login <password>");
            return true;
        }
        
        String password = args[0];
        
        // Check if player is already logged in
        if (!storageManager.playerExists(player.getUniqueId())) {
            messageUtils.sendMessage(player, "&c&l✗ &7You are not registered! &7Use &e/register <password> <password>");
            return true;
        }
        
        boolean success = authManager.loginPlayer(player, password);
        
        // Don't send success message again as it's handled in AuthManager
        if (!success) {
            // Failed login message is handled in AuthManager
        }
        
        return true;
    }
    
    private boolean handleRegisterCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "&c&l✗ &7This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length != 2) {
            messageUtils.sendMessage(player, "&c&l✗ &7Usage: &e/register <password> <password>");
            return true;
        }
        
        String password = args[0];
        String confirmPassword = args[1];
        
        boolean success = authManager.registerPlayer(player, password, confirmPassword);
        
        return success;
    }
    
    private boolean handleTurboAuthCommand(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        if (!PermissionUtils.hasPermission(player, "turboauth.admin")) {
            messageUtils.sendMessage(sender, "&c&l✗ &7You don't have permission to use this command!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                return handleReloadCommand(sender);
            case "setspawn":
                return handleSetSpawnCommand(sender, args);
            case "setfallback":
                return handleSetFallbackCommand(sender, args);
            case "info":
                return handleInfoCommand(sender);
            case "help":
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        try {
            // Reload configuration
            configManager.loadConfig();
            
            // Reload data
            storageManager.loadData();
            
            messageUtils.sendMessage(sender, "&a&l✓ &7Configuration and data reloaded successfully!");
            plugin.getLogger().info("TurboAuth configuration and data reloaded by " + 
                (sender instanceof Player ? ((Player) sender).getName() : "Console"));
            
        } catch (Exception e) {
            messageUtils.sendMessage(sender, "&c&l✗ &7Error reloading: &e" + e.getMessage());
            plugin.getLogger().severe("Error reloading TurboAuth: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleSetSpawnCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "&c&l✗ &7This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        configManager.setAuthSpawn(location);
        
        messageUtils.sendMessage(player, "&a&l✓ &7Auth spawn set to your current location!");
        plugin.getLogger().info("Auth spawn set to " + location.getWorld().getName() + 
            " (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ") by " + 
            player.getName());
        
        return true;
    }
    
    private boolean handleSetFallbackCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendMessage(sender, "&c&l✗ &7This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        Location location = player.getLocation();
        
        configManager.setFallbackSpawn(location);
        
        messageUtils.sendMessage(player, "&a&l✓ &7Fallback spawn set to your current location!");
        plugin.getLogger().info("Fallback spawn set to " + location.getWorld().getName() + 
            " (" + location.getX() + ", " + location.getY() + ", " + location.getZ() + ") by " + 
            player.getName());
        
        return true;
    }
    
    private boolean handleInfoCommand(CommandSender sender) {
        messageUtils.sendMessage(sender, "&e&l=== &6TurboAuth &e&l===");
        messageUtils.sendMessage(sender, "&7Version: &e" + plugin.getDescription().getVersion());
        messageUtils.sendMessage(sender, "&7Registered Players: &e" + storageManager.playerDataSize());
        messageUtils.sendMessage(sender, "&7Plugin Author: &eTurboAuth Team");
        messageUtils.sendMessage(sender, "&e&l==================");
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        messageUtils.sendMessage(sender, "&e&l=== &6TurboAuth Help &e&l===");
        messageUtils.sendMessage(sender, "&7/login <password> &8- &7Login to the server");
        messageUtils.sendMessage(sender, "&7/register <password> <password> &8- &7Register a new account");
        messageUtils.sendMessage(sender, "&7/turboauth reload &8- &7Reload plugin configuration");
        messageUtils.sendMessage(sender, "&7/turboauth setspawn &8- &7Set authentication spawn location");
        messageUtils.sendMessage(sender, "&7/turboauth setfallback &8- &7Set post-login fallback spawn");
        messageUtils.sendMessage(sender, "&7/turboauth info &8- &7Show plugin information");
        messageUtils.sendMessage(sender, "&7/turboauth help &8- &7Show this help message");
        messageUtils.sendMessage(sender, "&e&l==================");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (cmd.getName().equalsIgnoreCase("turboauth")) {
            if (args.length == 1) {
                Player player = sender instanceof Player ? (Player) sender : null;
                if (PermissionUtils.hasPermission(player, "turboauth.admin")) {
                    completions.add("reload");
                    completions.add("setspawn");
                    completions.add("setfallback");
                    completions.add("info");
                    completions.add("help");
                }
            }
        }
        
        return completions;
    }
}