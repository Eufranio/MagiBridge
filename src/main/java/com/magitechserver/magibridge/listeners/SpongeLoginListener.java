package com.magitechserver.magibridge.listeners;

import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.NucleusHandler;
import com.magitechserver.magibridge.util.GroupUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 10/07/2017.
 */
public class SpongeLoginListener {

    @Listener(order = Order.LAST)
    public void onLogin(ClientConnectionEvent.Join event, @First Player p) {
        if(!p.hasPlayedBefore()) {
            DiscordHandler.sendMessageToChannel(MagiBridge.getConfig().getString("channel", "main-discord-channel"), MagiBridge.getConfig().getString("messages", "new-players-message").replace("%player%", p.getName()));
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", p.getName());
        placeholders.put("%nick%", NucleusHandler.getNick(p));
        placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
        placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));

        DiscordHandler.sendMessageToDiscord(MagiBridge.getConfig().getString("channel", "main-discord-channel"),
                "player-join-message",
                placeholders,
                false,
                0);
    }

    @Listener(order = Order.LAST)
    public void onQuit(ClientConnectionEvent.Disconnect event, @First Player p) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", p.getName());
        placeholders.put("%nick%", NucleusHandler.getNick(p));
        placeholders.put("%prefix%", p.getOption("prefix").orElse(""));
        placeholders.put("%topgroup%", GroupUtil.getHighestGroup(p));

        DiscordHandler.sendMessageToDiscord(MagiBridge.getConfig().getString("channel", "main-discord-channel"),
                "player-quit-message",
                placeholders,
                false,
                0);
    }

}
