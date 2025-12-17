package com.turboauth.utils;

import com.turboauth.TurboAuth;
import com.turboauth.config.ConfigManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimationUtils {

    private final TurboAuth plugin;
    private final ConfigManager configManager;
    private final MessageUtils messageUtils;

    public AnimationUtils(TurboAuth plugin, ConfigManager configManager, MessageUtils messageUtils) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.messageUtils = messageUtils;
    }

    public void showJoinAnimation(Player player) {
        String title = configManager.getConfig().getString("animations.join.title");
        String subtitle = configManager.getConfig().getString("animations.join.subtitle");
        int fadeIn = configManager.getConfig().getInt("animations.join.fade-in");
        int stay = configManager.getConfig().getInt("animations.join.stay");
        int fadeOut = configManager.getConfig().getInt("animations.join.fade-out");

        if (title != null && !title.trim().isEmpty()) {
            messageUtils.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }

        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.join"));
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }
    }

    public void showSuccessAnimation(Player player) {
        String title = configManager.getConfig().getString("animations.success.title");
        String subtitle = configManager.getConfig().getString("animations.success.subtitle");
        int fadeIn = configManager.getConfig().getInt("animations.success.fade-in");
        int stay = configManager.getConfig().getInt("animations.success.stay");
        int fadeOut = configManager.getConfig().getInt("animations.success.fade-out");

        if (title != null && !title.trim().isEmpty()) {
            messageUtils.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void showErrorAnimation(Player player) {
        if (configManager.getConfig().getBoolean("sounds.enabled")) {
            try {
                Sound sound = Sound.valueOf(configManager.getConfig().getString("sounds.login-fail"));
                player.playSound(player.getLocation(), sound, 1.0f, 0.5f);
            } catch (IllegalArgumentException e) {
                // Invalid sound, skip
            }
        }
    }

    public void showGradientAnimation(Player player) {
        new BukkitRunnable() {
            private int tick = 0;
            private final String[] colors = {
                "<red>", "<orange>", "<yellow>", "<green>", "<blue>", "<purple>", "<pink>", "<cyan>"
            };

            @Override
            public void run() {
                if (tick >= 40 || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                String color = colors[tick % colors.length];
                String animatedMessage = color + "&l⟶ &7&lPlease authenticate with &e&l/register <password> <password>";

                messageUtils.sendActionBar(player, animatedMessage);
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
