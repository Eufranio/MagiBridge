package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.util.GroupUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 14/07/2017.
 */
public class DeathListener {

    @Listener(order = Order.LAST)
    public void onDeath(DestructEntityEvent.Death event) {
        if (event.getMessage().toPlain().isEmpty()) return;
        if (event.getTargetEntity() instanceof Player) {
            Player p = (Player)event.getTargetEntity();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player%", p.getName());
            placeholders.put("%nick%", NucleusHandler.getNick(p));
            placeholders.put("%deathmessage%", event.getMessage().toPlain());
            placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
            placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));

            DiscordHandler.sendMessageToDiscord(MagiBridge.getConfig().getString("channel", "main-discord-channel"),
                    "death-message",
                    placeholders,
                    false,
                    0);
        }
    }
}
