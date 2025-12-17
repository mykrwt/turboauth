package com.turboauth.utils;

import com.turboauth.config.ConfigManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    private final ConfigManager configManager;

    public MessageUtils(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public String colorize(String message) {
        if (message == null) {
            return "";
        }

        message = message.replaceAll("&", "§");
        message = message.replaceAll("§#([A-Fa-f0-9]{6})", "<#$1>");

        message = message.replaceAll("§0", "<black>");
        message = message.replaceAll("§1", "<dark_blue>");
        message = message.replaceAll("§2", "<dark_green>");
        message = message.replaceAll("§3", "<dark_aqua>");
        message = message.replaceAll("§4", "<dark_red>");
        message = message.replaceAll("§5", "<dark_purple>");
        message = message.replaceAll("§6", "<gold>");
        message = message.replaceAll("§7", "<gray>");
        message = message.replaceAll("§8", "<dark_gray>");
        message = message.replaceAll("§9", "<blue>");
        message = message.replaceAll("§a", "<green>");
        message = message.replaceAll("§b", "<aqua>");
        message = message.replaceAll("§c", "<red>");
        message = message.replaceAll("§d", "<light_purple>");
        message = message.replaceAll("§e", "<yellow>");
        message = message.replaceAll("§f", "<white>");

        message = message.replaceAll("§k", "<obfuscated>");
        message = message.replaceAll("§l", "<bold>");
        message = message.replaceAll("§m", "<strikethrough>");
        message = message.replaceAll("§n", "<underline>");
        message = message.replaceAll("§o", "<italic>");
        message = message.replaceAll("§r", "<reset>");

        return message;
    }

    public void sendMessage(Player player, String message) {
        if (player == null || message == null || message.trim().isEmpty()) {
            return;
        }

        String coloredMessage = colorize(message);
        player.sendMessage(coloredMessage);
    }

    public void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null || message.trim().isEmpty()) {
            return;
        }

        String coloredMessage = colorize(message);
        sender.sendMessage(coloredMessage);
    }

    public void sendActionBar(Player player, String message) {
        if (player == null || message == null || message.trim().isEmpty()) {
            return;
        }

        String coloredMessage = colorize(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(coloredMessage));
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) {
            return;
        }

        if (title != null && !title.trim().isEmpty()) {
            String coloredTitle = colorize(title);
            player.sendTitle(coloredTitle, subtitle != null ? colorize(subtitle) : null, fadeIn, stay, fadeOut);
        }
    }

    public void sendJoinMessage(Player player, boolean registered) {
        if (configManager == null) {
            return;
        }

        String message = registered
            ? configManager.getMessage("messages.join-registered")
            : configManager.getMessage("messages.join-not-registered");

        sendMessage(player, message);
    }

    public void sendReminderMessage(Player player) {
        if (configManager == null) {
            return;
        }

        String message = configManager.getMessage("messages.reminder");
        sendMessage(player, message);
        sendActionBar(player, message);
    }

    public void sendSuccessMessage(Player player) {
        if (configManager == null) {
            return;
        }

        sendMessage(player, configManager.getMessage("messages.login-success"));
    }
}
