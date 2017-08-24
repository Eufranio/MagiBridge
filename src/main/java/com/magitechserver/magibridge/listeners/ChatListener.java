package com.magitechserver.magibridge.listeners;

import br.net.fabiozumbi12.UltimateChat.API.SendChannelMessageEvent;
import com.magitechserver.magibridge.DiscordHandler;
import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.util.GroupUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frani on 05/07/2017.
 */
public class ChatListener {

    @Listener
    public void onMessage(SendChannelMessageEvent e) {
        // Tell
        if(e.getChannel() == null) return;

        String channel = getKey(e.getChannel().getName().toLowerCase());
        if(channel == null) return;
        String format = "server-to-discord-format";
        Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%prefix%", e.getSender().getOption("prefix").orElse(""));
            placeholders.put("%player%", e.getSender().getName());
            placeholders.put("%message%", e.getMessage().toPlain());
            placeholders.put("%topgroup%", GroupUtil.getHighestGroup((Player)e.getSender()));
        boolean removeEveryone = !e.getSender().hasPermission("magibridge.everyone");

        DiscordHandler.sendMessageToDiscord(channel, format, placeholders, removeEveryone, 0);
    }

    private String getKey(String value) {
        for (Map.Entry<String, String> values : MagiBridge.getConfig().getMap("channel", "ultimatechat").entrySet()) {
            if(value.equals(values.getValue().toLowerCase())) {
                return values.getKey();
            }
        }
        return null;
    }
}

