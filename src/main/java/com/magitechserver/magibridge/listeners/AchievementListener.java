package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;

/**
 * Created by Frani on 14/07/2017.
 */
public class AchievementListener {

    @Listener(order = Order.LAST)
    public void onAchievement(GrantAchievementEvent.TargetPlayer event) {
        String player = event.getTargetEntity().getName();
        String achievement = event.getAchievement().getName();
        String msg = MagiBridge.getConfig().getString("messages", "achievement-message")
                .replace("%player%", player)
                .replace("%achievement%", achievement);
        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"), msg);
    }
}
