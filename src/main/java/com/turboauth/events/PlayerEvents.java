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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerEvents implements Listener {

    private final AuthManager authManager;
    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final MessageUtils messageUtils;
    private final AnimationUtils animationUtils;

    public PlayerEvents(TurboAuth plugin, AuthManager authManager, ConfigManager configManager, StorageManager storageManager) {
        this.authManager = authManager;
        this.configManager = configManager;
        this.storageManager = storageManager;

        this.messageUtils = new MessageUtils(configManager);
        this.animationUtils = new AnimationUtils(plugin, configManager, messageUtils);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        boolean isRegistered = storageManager.playerExists(player.getUniqueId());

        authManager.savePlayerLocationAndRestrict(player);

        messageUtils.sendJoinMessage(player, isRegistered);
        animationUtils.showJoinAnimation(player);

        if (!configManager.isAutoLogin() && isRegistered) {
            authManager.startReminderTask(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        authManager.stopReminderTask(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!isAuthenticated(player) && configManager.isFreezeEnabled()) {
            event.setTo(event.getFrom());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!isAuthenticated(player)) {
            String message = event.getMessage().toLowerCase();

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

        if (!isAuthenticated(player)) {
            messageUtils.sendMessage(player, "&c&l✗ &7You must authenticate first!");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
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
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!isAuthenticated(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if (!isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!isAuthenticated(player)) {
            org.bukkit.Location authSpawn = configManager.getAuthSpawn();
            if (authSpawn != null && event.getTo() != null && !event.getTo().equals(authSpawn)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isAuthenticated(Player player) {
        return !storageManager.getSavedLocationMap().containsKey(player.getUniqueId());
    }
}
