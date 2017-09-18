package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.util.GroupUtil;
import com.magitechserver.magibridge.util.ReplacerUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Frani on 14/07/2017.
 */
public class AchievementListener {

    @Listener(order = Order.LAST)
    public void onAchievement(GrantAchievementEvent.TargetPlayer event, @Root Player p) {
        Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", p.getName());
            placeholders.put("%nick%", NucleusHandler.getNick(p));
            placeholders.put("%achievement%", event.getAchievement().getName());
            placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
            placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));

        DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"),
                ReplacerUtil.replaceEach(MagiBridge.getConfig().getString("messages", "achievement-message"), placeholders));
    }
}
