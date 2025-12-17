package com.turboauth.events;

import com.turboauth.TurboAuth;
import com.turboauth.auth.AuthManager;
import com.turboauth.config.ConfigManager;
import com.turboauth.storage.StorageManager;
import com.turboauth.utils.AnimationUtils;
import com.turboauth.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

public class PlayerEvents implements Listener {
    
    private final TurboAuth plugin;
    private final AuthManager authManager;
    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final MessageUtils messageUtils;
    private final AnimationUtils animationUtils;
    
    public PlayerEvents() {
        this.plugin = TurboAuth.getInstance();
        this.authManager = plugin.getAuthManager();
        this.configManager = plugin.getConfigManager();
        this.storageManager = plugin.getStorageManager();
        this.messageUtils = new MessageUtils();
        this.animationUtils = new AnimationUtils();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is registered
        boolean isRegistered = storageManager.playerExists(player.getUniqueId());
        
        // Save player location and apply restrictions
        authManager.savePlayerLocationAndRestrict(player);
        
        // Send appropriate join message
        messageUtils.sendJoinMessage(player, isRegistered);
        
        // Show join animation
        animationUtils.showJoinAnimation(player);
        
        // Start reminder task if not auto-login
        if (!configManager.isAutoLogin() && isRegistered) {
            authManager.startReminderTask(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Stop reminder task
        authManager.stopReminderTask(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            // Cancel movement if freeze is enabled
            if (configManager.isFreezeEnabled()) {
                event.setTo(event.getFrom());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            String message = event.getMessage().toLowerCase();
            
            // Allow only login and register commands
            if (!message.startsWith("/login") && 
                !message.startsWith("/register") &&
                !message.startsWith("/l ") &&
                !message.startsWith("/reg ")) {
                
                messageUtils.sendMessage(player, "&c&l✗ &7You must authenticate first!");
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            messageUtils.sendMessage(player, "&c&l✗ &7You must authenticate first!");
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
            player.closeInventory();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // Check if player is authenticated
        if (!isAuthenticated(player)) {
            // Allow teleportation to auth spawn, otherwise cancel
            org.bukkit.Location authSpawn = configManager.getAuthSpawn();
            if (authSpawn != null && !event.getTo().equals(authSpawn)) {
                event.setCancelled(true);
            }
        }
    }
    
    private boolean isAuthenticated(Player player) {
        // Player is considered authenticated if they are NOT in the saved locations map
        // This means they have been successfully restored and their location entry was removed
        return !storageManager.getSavedLocationMap().containsKey(player.getUniqueId());
    }
}