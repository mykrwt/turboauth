package com.turboauth.utils;

import org.bukkit.entity.Player;

public class PermissionUtils {
    
    public static boolean hasPermission(Player player, String permission) {
        if (player == null) return false;
        
        // Admin permission check
        if (permission.equals("turboauth.admin")) {
            return player.hasPermission("turboauth.*") || player.hasPermission("turboauth.admin");
        }
        
        // Reload permission check
        if (permission.equals("turboauth.reload")) {
            return player.hasPermission("turboauth.*") || player.hasPermission("turboauth.admin");
        }
        
        // Basic permissions
        return player.hasPermission(permission) || player.hasPermission("turboauth.*");
    }
    
    public static boolean hasPermissionOrOp(Player player, String permission) {
        if (player == null) return false;
        return player.isOp() || hasPermission(player, permission);
    }
}